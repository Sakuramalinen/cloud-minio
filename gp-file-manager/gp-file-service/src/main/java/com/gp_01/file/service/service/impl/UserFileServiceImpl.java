package com.gp_01.file.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gp_01.common.context.FileDownloadContext;
import com.gp_01.common.context.UserContext;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.common.enums.FileTypeEnum;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.common.exception.BizIllegalException;
import com.gp_01.common.exception.ForbiddenException;
import com.gp_01.common.utils.FileTypeResolver;
import com.gp_01.common.utils.TimeUtils;
import com.gp_01.file.model.domain.dto.DownloadFileDTO;
import com.gp_01.file.model.domain.dto.PreviewFileDTO;
import com.gp_01.file.model.domain.dto.UploadFileDTO;
import com.gp_01.file.model.domain.po.FileBase;
import com.gp_01.file.model.domain.po.UserFile;
import com.gp_01.file.model.domain.query.PageFilesQuery;
import com.gp_01.file.model.domain.vo.FileDetail;
import com.gp_01.file.model.domain.vo.ListRecycleBinVO;
import com.gp_01.file.model.domain.vo.PreviewImagesVO;
import com.gp_01.file.model.domain.vo.UploadVO;
import com.gp_01.file.service.config.FileManagerServiceProperties;
import com.gp_01.file.service.config.MinioConfig;
import com.gp_01.file.service.mapper.UserFileMapper;
import com.gp_01.file.service.operation.download.domian.DownloadFile;
import com.gp_01.file.service.operation.download.product.MinioDownloader;
import com.gp_01.file.service.operation.upload.domain.UploadFile;
import com.gp_01.file.service.operation.upload.domain.UploadFileResult;
import com.gp_01.file.service.operation.upload.product.MinioUploader;
import com.gp_01.file.service.service.IFileBaseService;
import com.gp_01.file.service.service.IUserFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gp_01.file.service.util.FileUtils;
import com.gp_01.file.service.util.HttpUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.gp_01.common.enums.FileTypeEnum.*;

/**
 * <p>
 * 用户逻辑文件表 服务实现类
 * </p>
 *
 * @author employee_01
 * @since 2026-05-08
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserFileServiceImpl extends ServiceImpl<UserFileMapper, UserFile> implements IUserFileService {

    private final IFileBaseService fileBaseService;

    private final UserFileMapper userFileMapper;

    private final FileManagerServiceProperties fileManagerServiceProperties;


    private final MinioConfig minioConfig;

    private final FileUtils fileUtils;

    @Override
    @Deprecated
    public void uploadFile(MultipartFile file, Long parentId, String md5Hex) {
        Long userId = UserContext.getUser();
        String fileName = file.getOriginalFilename();
        boolean exist = fileExist(userId, parentId, fileName);
        if (exist) {
            throw new BadRequestException("文件已存在");
        }
        if (StringUtils.isEmpty(fileName)) {
            throw new BadRequestException("文件名异常");
        }
        //准备数据
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //上传源文件
        FileBase fileBase = fileBaseService.uploadOriginalFile(file, md5Hex);

        //组装数据
        UserFile userFile = new UserFile();
        userFile.setUserId(userId);
        userFile.setFileId(fileBase.getId());
        userFile.setParentId(parentId);
        userFile.setFileName(fileName);
        userFile.setFileSuffix(suffix);
        userFile.setFileSize(file.getSize());
        userFile.setContentType(file.getContentType());
        userFile.setFileType(FileTypeResolver.parse(file.getContentType()));
        super.save(userFile);

        //TODO 异步制作缩略图
        if (fileBase.getContentType().split("/")[0].equals("image")) {
            fileBaseService.uploadThumbnailsFile(fileBase);
        }
    }

    @Override
    public void makeDir(Long parentId, String fileName) {
        Long userId = UserContext.getUser();
        //判断是否存在
        boolean exist = fileExist(userId, parentId, fileName);
        if (exist) {
            throw new BadRequestException("该文件夹已存在");
        }
        //组装数据
        UserFile userFile = new UserFile();
        userFile.setUserId(userId);
        userFile.setParentId(parentId);
        userFile.setFileName(fileName);
        userFile.setFileType(DIRECTORY);

        //保存到数据库
        super.save(userFile);
    }

    @Override
    public void reName(Long id, String fileName) {
        Long userId = UserContext.getUser();
        //查数据
        UserFile one = lambdaQuery().eq(UserFile::getId, id).one();
        if (one == null) {
            throw new BadRequestException("空数据");
        }
        if (!Objects.equals(one.getUserId(), userId)) {
            throw new ForbiddenException("用户无权限操作");
        }
        //判断是否有同名文件
        boolean exist = existSameFileName(userId, one.getParentId(), fileName);
        if (exist) {
            throw new BadRequestException("该目录存在同名文件");
        }
        //修改
        lambdaUpdate()
                .eq(UserFile::getId, id)
                .set(UserFile::getFileName, fileName)
                .set(UserFile::getUpdateTime, LocalDateTime.now())
                .update();
    }

    private boolean existSameFileName(Long userid, Long parentId, String fileName) {
        UserFile one = lambdaQuery()
                .eq(UserFile::getUserId, userid)
                .eq(UserFile::getParentId, parentId)
                .eq(UserFile::getFileName, fileName)
                .one();
        return one != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        Long userId = UserContext.getUser();
        //查数据
        UserFile one = lambdaQuery().eq(UserFile::getId, id).one();
        if (one == null) {
            throw new BadRequestException("空数据");
        }
        if (!Objects.equals(one.getUserId(), userId)) {
            throw new ForbiddenException("用户无权限操作");
        }
        Long timeStamp = Instant.now().toEpochMilli();

        //逻辑删除
        super.lambdaUpdate()
                .eq(UserFile::getId, id)
                .eq(UserFile::getUserId, userId)
                .set(UserFile::getDeleted, timeStamp)
                .update();

        //TODO 可以异步 物理文件表的该文件引用-1 :这个功能需要定时任务接管
    }

    @Override
    public PageResult<UserFile> listFileByParentId(PageFilesQuery query) {
        Long userId = UserContext.getUser();
        //条件分页查询
//        Page<UserFile> page = lambdaQuery()
//                .eq(UserFile::getFileId, query.getId())
//                .eq(UserFile::getUserId, userId)
//                .eq(UserFile::getDeleted, 0)
//                .orderBy(StringUtils.isNotEmpty(query.getSortBy()), query.getIsAsc(), UserFile.getSortByColumn(query.getSortBy()))
//                .page(query.toPage());
        Page<UserFile> page = query.toPage();
        //条件分页查询
        userFileMapper.listFileByPage(page, query, userId);
        //如果没数据返回空集合
        if (page.getRecords().isEmpty()) {
            return PageResult.empty(page);
        }

        return PageResult.of(page);
    }


    @Override
    public void downloadById(Long id, HttpServletResponse response) {
        Long userId = UserContext.getUser();
        UserFile file = super.lambdaQuery()
                .eq(UserFile::getId, id)
                .eq(UserFile::getUserId, userId)
                .one();
        if (file == null) {
            throw new BadRequestException("文件不存在");
        }
        ContentDisposition attachment = ContentDisposition.attachment().filename(file.getFileName(), StandardCharsets.UTF_8).build();
        //设置响应信息
        response.reset();
        response.setHeader("Content-Disposition", attachment.toString());
        response.setCharacterEncoding("utf-8");
        response.setContentLengthLong(file.getFileSize());
        response.setContentType(file.getContentType());

        if (file.getFileType() == DIRECTORY) {
            //文件夹下载
            DirToZipDownload(id, userId, response);
        } else {
            //文件下载
            fileBaseService.fileDownload(file, response);
        }
    }

    @Override
    public void previewFileById(String id, HttpServletResponse response) {
        Long userId = UserContext.getUser();
        UserFile file = super.lambdaQuery()
                .eq(UserFile::getId, id)
                .eq(UserFile::getUserId, userId)
                .one();
        if (file == null) {
            throw new BadRequestException("文件不存在");
        }
        if (file.getFileType() == DIRECTORY) {
            throw new BadRequestException("文件夹不可预览");
        }

        //设置响应信息
        ContentDisposition inline = ContentDisposition
                .inline()
                .filename(file.getFileName(), StandardCharsets.UTF_8)
                .build();
        response.reset();
        response.setHeader("Content-Disposition", inline.toString());
        response.setContentType(file.getContentType());
        response.setCharacterEncoding("utf-8");
        //下载文件到响应体
        fileBaseService.fileDownload(file, response);

    }

    @Override
    public List<ListRecycleBinVO> listRecycleBin() {
        Long userId = UserContext.getUser();
        //TODO 提取到配置文件 获得30天的时间戳
        long limitTime = Instant.now().minusSeconds(60 * 60 * 24 * 30).toEpochMilli();
        //查数据库
        List<UserFile> list = lambdaQuery()
                .eq(UserFile::getUserId, userId)
                .ge(UserFile::getDeleted, limitTime)
                .list();
        if (list == null) {
            return List.of();
        }
        List<ListRecycleBinVO> res = new ArrayList<>();
        for (UserFile userFile : list) {
            ListRecycleBinVO vo = new ListRecycleBinVO();
            BeanUtils.copyProperties(userFile, vo);
            //设置独有属性
            LocalDateTime deleteTime = TimeUtils.milliToLocalDateTime(userFile.getDeleted());
            long validDay = 14 - ChronoUnit.DAYS.between(deleteTime, LocalDateTime.now());
            vo.setValidDay(validDay);
            vo.setDeleteTime(deleteTime);
            res.add(vo);
        }
        return res;

    }

    @Override
    public void restoreFile(List<Long> ids) {
        if (ids == null) {
            throw new BadRequestException("请求参数为空");
        }
        Long userId = UserContext.getUser();
        //修改数据库 逻辑删除为0
        lambdaUpdate()
                .in(UserFile::getId, ids)
                .eq(UserFile::getUserId, userId)
                .set(UserFile::getDeleted, 0)
                .update();
    }

    @Override
    public PageResult<PreviewImagesVO> pagePreviewImages(PageParams params) {
        //获取登录用户
        Long userId = UserContext.getUser();
        //条件分页查询
        Page<UserFile> page = lambdaQuery()
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getFileType, IMAGE)
                .eq(UserFile::getDeleted, 0)
                .page(params.toPage());
        //获取分页数据
        List<UserFile> records = page.getRecords();
        if (records.isEmpty()) {
            return PageResult.empty();
        }
        //收集文件id
        List<Long> fileIds = new ArrayList<>();
        for (UserFile record : records) {
            fileIds.add(record.getFileId());
        }
        //根据文件id查询文件基本信息
        Map<Long, FileBase> fileBaseMap = fileBaseService.lambdaQuery()
                .in(FileBase::getId, fileIds)
                .list().stream().collect(Collectors.toMap(FileBase::getId, fileBase -> fileBase));
        //返回结果集合
        List<PreviewImagesVO> res = new ArrayList<>();
        for (UserFile record : records) {
            PreviewImagesVO vo = new PreviewImagesVO();
            //bean拷贝
            BeanUtils.copyProperties(record, vo);
            //组装剩余属性
            int year = record.getCreateTime().getYear();
            int month = record.getCreateTime().getMonthValue();
            int day = record.getCreateTime().getDayOfMonth();
            String objectPath = fileBaseMap.get(record.getFileId()).getObjectPath();
            //获取临时签名url
            String[] urls = fileBaseService.getTempSignedUrl(objectPath, minioConfig.getTempSignedUrlExpireMinute());
            vo.setThumbUrl(urls[1]);
            vo.setOriginalUrl(urls[0]);
            vo.setYear(year);
            vo.setMonth(month);
            vo.setDay(day);
            res.add(vo);
        }
        return new PageResult<>(page.getTotal(), page.getSize(), page.getCurrent(), res);
    }

    @Override
    public PageResult<UserFile> listFileByTypeAndPage(PageParams params, Integer type) {
        //获取登录用户
        Long userId = UserContext.getUser();
        //条件查询
        Page<UserFile> page = lambdaQuery()
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getFileType, type)
                .eq(UserFile::getDeleted, 0)
                .page(params.toPage());

        List<UserFile> records = page.getRecords();
        if (records.isEmpty()) {
            return PageResult.empty(page);
        }
        return PageResult.of(page);
    }

    @Override
    public void moveFile(Long fileId, Long targetId) {
        Long userId = UserContext.getUser();
        UserFile one = super.lambdaQuery()
                .eq(UserFile::getId, fileId)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getDeleted, 0)
                .one();
        if (one == null) {
            throw new BadRequestException("文件不存在");
        }
        //判断目标目录上有没有同名文件
        Integer cnt = userFileMapper.existsSameFileName(fileId, userId, targetId);
        if (cnt != 0) {
            throw new BadRequestException("目标目录存在同名文件");
        }
        //更改该文件的文件夹id
        one.setParentId(targetId);
        super.updateById(one);
    }

    @Override
    public List<UserFile> listDirByParentId(Long parentId) {
        Long userId = UserContext.getUser();
        List<UserFile> list = super.lambdaQuery()
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getDeleted, 0)
                .eq(UserFile::getParentId, parentId)
                .eq(UserFile::getFileType, DIRECTORY)
                .list();
        if (list == null) {
            return List.of();
        }
        return list;
    }

    private final MinioUploader minioUploader;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadVO uploadFile(MultipartFile file, UploadFileDTO uploadFileDTO) {

        Long userId = UserContext.getUser();
        String fileName = uploadFileDTO.getFileName();
        FileBase fileBase = null;
        boolean exist = fileExist(userId, uploadFileDTO.getParentFileId(), fileName);
        if (uploadFileDTO.getCurrentChunkIndex() == 1) {
            //1.判断该用户当前文件夹内是否有同名文件
            if (exist) {
                throw new BadRequestException("文件已存在");
            }
            if (StringUtils.isEmpty(fileName)) {
                throw new BadRequestException("文件名异常");
            }
            //判断是否上传过
            fileBase = fileBaseService.exist(uploadFileDTO.getFileMd5());
        }
        UploadVO vo = new UploadVO();

        //2. 上传文件
        if (fileBase != null) {
            //该文件被上传过， 引用+1
            fileBaseService.incrementRefCount(uploadFileDTO.getFileMd5());

        } else {
            //上传分片文件
            UploadFile uploadFile = new UploadFile();
            BeanUtils.copyProperties(uploadFileDTO, uploadFile);
            UploadFileResult uploadFileResult = minioUploader.uploadFileChunk(uploadFile, file);

            vo.setUploaded(uploadFileResult.getUploaded());
            vo.setProgress(uploadFileResult.getProgress());
        }
        //3.存数据库
        String extendName = fileUtils.getFileExtendName(fileName);
        LocalDateTime now = LocalDateTime.now();
        if (fileBase == null && vo.getUploaded()) {
            //准备数据
            fileBase = new FileBase();
            fileBase.setFileSize(uploadFileDTO.getFileSize());
            fileBase.setContentType(file.getContentType());
            fileBase.setBucketName(minioConfig.getBucketName());
            String objectPath = fileBaseService.getObjectPath(now, uploadFileDTO.getFileMd5(), extendName);
            fileBase.setObjectPath(objectPath);
            fileBase.setFileMd5(uploadFileDTO.getFileMd5());
            fileBase.setRefCount(1);
            fileBase.setCreateTime(now);
            //保存到数据库
            fileBaseService.save(fileBase);
        }

        //判断是否上传完成整个文件
        if (vo.getUploaded()) {
            //获取文件类型
            String integratePath = fileBaseService.getIntegratePath(now, uploadFileDTO.getFileMd5(), extendName);
            FileTypeEnum fileType = fileUtils.getFileType(integratePath, fileName);
            //准备数据
            UserFile userFile = new UserFile();
            userFile.setUserId(userId);
            userFile.setFileId(fileBase.getId());
            userFile.setParentId(uploadFileDTO.getParentFileId());
            userFile.setFileName(fileName);
            userFile.setFileSuffix(extendName);
            userFile.setFileSize(uploadFileDTO.getFileSize());
            userFile.setContentType(file.getContentType());
            userFile.setFileType(fileType);
            userFile.setCreateTime(now);
            userFile.setUpdateTime(now);
            userFile.setDeleted(0L);
            //保存到数据库
            super.save(userFile);
            vo.setFileId(userFile.getId());

            //图片制作缩略图
            if (fileType == IMAGE) {
                fileBaseService.uploadThumbnailsFile(fileBase);
            }
            if (fileType == VIDEO) {
                //TODO处理视频关键帧
            }
        }
        return vo;

    }

    //    private final Downloader downloader;
    private final MinioDownloader downloader;

    @Override
    public void downloadFile(HttpServletRequest request, HttpServletResponse response, DownloadFileDTO dto) {
        //获取文件下载地址
        String downloadPath = FileDownloadContext.getDownloadPath();
        if (downloadPath == null) {
            throw new ForbiddenException("无权限下载");
        }

        DownloadFile downloadFile;
        if (dto.getChunked()) {
            //分片下载
            Long[] range = HttpUtils.getRequestRange(request, dto.getFileSize());
            //设置下载相关请求头
            HttpUtils.setDownloadResponse(response, dto.getFileName(), dto.getFileSize(), dto.getContentType(), range[0], range[1]);
            downloadFile = new DownloadFile(downloadPath, range[0], range[1] - range[0] + 1, dto.getChunked());
        } else {
            //文件整体下载
            downloadFile = new DownloadFile(downloadPath);
            HttpUtils.setDownloadResponse(response, dto.getFileName(), dto.getFileSize(), dto.getContentType());

        }
        //下载文件
        try (InputStream is = downloader.execute(downloadFile);) {
            response.getOutputStream().write(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }

    @Override
    public FileDetail getFileDetail(Long id) {
        //判断是否有权限下载
        UserFile userFile = super.getById(id);

        Long userId = UserContext.getUser();
        if (userFile == null || !userFile.getUserId().equals(userId)) {
            throw new ForbiddenException("下载失败，暂无下载权限");
        }

        FileBase fileBase = fileBaseService.getById(userFile.getFileId());
        if (fileBase == null) {
            log.error("fileBase与userFile业务数据不一致");
            throw new BizIllegalException("文件不存在");
        }

        FileDetail fileDetail = new FileDetail();
        BeanUtils.copyProperties(userFile, fileDetail);
        fileDetail.setFileMd5(fileBase.getFileMd5());
        return fileDetail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecycleFileBatch(List<Long> ids) {
        Long userId = UserContext.getUser();
        //查询要被删除的fileId
        List<UserFile> list = super.lambdaQuery()
                .eq(UserFile::getUserId, userId)
                .in(UserFile::getId, ids)
                .ne(UserFile::getDeleted, 0).list();

        List<Long> fileIds = new ArrayList<>();
        for (UserFile userFile : list) {
            fileIds.add(userFile.getFileId());
        }
        //将该文件引用数-1
        fileBaseService.minusRefCountBatch(fileIds);

        //删除文件
        LambdaQueryWrapper<UserFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFile::getUserId, userId)
                .ne(UserFile::getDeleted, 0)
                .in(UserFile::getId, ids);
        super.remove(wrapper);



    }

    @Override
    public void previewFile(HttpServletRequest request, HttpServletResponse response, PreviewFileDTO dto) {
        Long userId = FileDownloadContext.getDownloadUser();
        if(!userId.equals(UserContext.getUser())){
            throw new ForbiddenException("暂无权限下载");
        }
        String previewPath = FileDownloadContext.getDownloadPath();

        //判断是否分流
        DownloadFile downloadFile = null;
        if (dto.getChunkStreamed()) {
            Long[] range = HttpUtils.getRequestRange(request, dto.getFileSize());
            //计算结束位置
            HttpUtils.setPreviewResponse(response, range[0], range[1], dto.getFileSize(), dto.getContentType());
            downloadFile = new DownloadFile(previewPath, range[0], range[1] - range[0] + 1,  dto.getChunkStreamed());
        } else {
            HttpUtils.setPreviewResponse(response, dto.getFileSize());
            downloadFile = new DownloadFile(previewPath);
        }

        //下载文件
        try(InputStream is = downloader.execute(downloadFile)){
            byte[] buff = new byte[1024 * 1024 * 5];
            int len;
            while((len = is.read(buff)) != -1){
                response.getOutputStream().write(buff,0, len);
            }
            log.debug("加载大小：{}", buff.length);
        }catch(Exception e){
            if(e.getMessage().contains("Broken pipe")){
                log.debug("链接断开");
            } else {
                throw new BadRequestException("文件下载失败");
            }
        }


    }


    //TODO 文件夹下载
    private void DirToZipDownload(Long id, Long userId, HttpServletResponse response) {
        throw new BadRequestException("还不支持文件夹下载");
    }

    /**
     * 判断文件或文件夹是否存在
     *
     * @param userId
     * @param parentId
     * @param fileName
     * @return
     */
    private boolean fileExist(Long userId, Long parentId, String fileName) {
        UserFile one = lambdaQuery()
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getParentId, parentId)
                .eq(UserFile::getFileName, fileName)
                .eq(UserFile::getDeleted, 0)
                .one();
        return one != null;
    }
}
