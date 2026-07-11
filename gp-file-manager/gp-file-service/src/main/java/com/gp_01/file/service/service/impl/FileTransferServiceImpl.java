package com.gp_01.file.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gp_01.common.context.FileDownloadContext;
import com.gp_01.common.context.UserContext;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.enums.FileTypeEnum;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.common.exception.CommonException;
import com.gp_01.common.exception.ForbiddenException;
import com.gp_01.common.exception.UnauthorizedException;
import com.gp_01.file.model.domain.dto.DownloadFileDTO;
import com.gp_01.file.model.domain.dto.PreviewFileDTO;
import com.gp_01.file.model.domain.dto.UploadFileDTO;
import com.gp_01.file.model.domain.po.FileBase;
import com.gp_01.file.model.domain.po.UserFile;
import com.gp_01.file.model.domain.vo.DownloadInfoVO;
import com.gp_01.file.model.domain.vo.PreviewImagesVO;
import com.gp_01.file.model.domain.vo.UploadVO;
import com.gp_01.file.service.config.MinioConfig;
import com.gp_01.file.service.mapper.FileBaseMapper;
import com.gp_01.file.service.mapper.UserFileMapper;
import com.gp_01.file.service.operation.download.Downloader;
import com.gp_01.file.service.operation.download.domain.DownloadFile;
import com.gp_01.file.service.operation.upload.Uploader;
import com.gp_01.file.service.operation.upload.domain.UploadFile;
import com.gp_01.file.service.operation.upload.domain.UploadFileResult;
import com.gp_01.file.service.service.IFileBaseService;
import com.gp_01.file.service.service.IFileTransferService;
import com.gp_01.file.service.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gp_01.common.enums.FileTypeEnum.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileTransferServiceImpl implements IFileTransferService {

    private final Downloader downloader;

    private final Uploader uploader;

    private final RedisUtils redisUtils;

    private final FileUtils fileUtils;

    private final UserFileMapper userFileMapper;

    private final FileBaseMapper fileBaseMapper;

    private final MinioConfig minioConfig;

    private final MinioUtils minioUtils;

    private final ThumbnailUtils thumbnailUtils;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadVO uploadFile(MultipartFile file, UploadFileDTO dto) {
        Long userId = UserContext.getUser();
        UploadVO vo = new UploadVO();

        String key = "gp_01:file:upload:" + userId + ":" + dto.getFileMd5();
        String cache = redisUtils.get(key);
        if (cache == null) {
            //1.第一个分片上传，进行初始化处理
            cache = uploadInit(dto, key);
        }
        boolean isNewFile = cache.equals("new");
        if (isNewFile) {
            //2. 上传分片文件
            if (vo.getUploaded() == null || !vo.getUploaded()) {
                UploadFileResult uploadFileResult = uploadExecute(dto, file);
                vo.setUploaded(uploadFileResult.getUploaded());
                vo.setProgress(uploadFileResult.getProgress());
            }
        }else {
            vo.setUploaded(true);
        }
        //如果还没上传完整，直接返回不存数据库
        if (!vo.getUploaded()) {
            return vo;
        }
        //只有最后一个分片上传才开始存数据
        if(!dto.getChunkNumber().equals(dto.getCurrentChunkIndex())){
            return vo;
        }
        //获取文件类型
        FileBase fileBase;
        if (isNewFile) {
            String extendName = fileUtils.getFileExtendName(dto.getFileName());
            String storePath = fileUtils.getOriginalFileStorePath(dto.getFileMd5(), extendName);
            String contentType = fileUtils.getFileType(storePath, dto.getFileName());
            FileTypeEnum fileTypeEnum = getFileTypeEnum(contentType);
            //数据持久化
            fileBase = uploadFileBasePersistence(dto, contentType, storePath);
            //文件后期处理
            postProduction(fileTypeEnum, dto, storePath);
        } else {
            LambdaQueryWrapper<FileBase> wrapper = new LambdaQueryWrapper<FileBase>().eq(FileBase::getFileMd5, dto.getFileMd5());
            fileBase = fileBaseMapper.selectOne(wrapper);
            //引用次数增加
            fileBaseMapper.incrementRefCount(dto.getFileMd5());
        }
        //数据持久化
        UserFile userFile = uploadUserFilePersistence(fileBase, dto);
        vo.setFileId(userFile.getFileId());

        redisUtils.deletedKey(key);

        return vo;
    }

    private String uploadInit(UploadFileDTO dto, String key){
        String cache = null;
        Long userId = UserContext.getUser();
        //.1判断是否上传过
        LambdaQueryWrapper<FileBase> wrapper = new LambdaQueryWrapper<FileBase>().eq(FileBase::getFileMd5, dto.getFileMd5());
        FileBase fileBase = fileBaseMapper.selectOne(wrapper);

        if (fileBase != null) {
            //该文件被上传过， 引用+1
            fileBaseMapper.incrementRefCount(dto.getFileMd5());
            cache = "old";
        }
        //.2判断该用户当前文件夹内是否有同名文件
        UserFile exist = fileNameExist(userId, dto.getParentFileId(), dto.getFileName());
        if (exist != null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "文件名重复");
        }
        cache = cache == null ? "new" : cache;
        redisUtils.set(key, cache);
        return cache;
    }


    private UploadFileResult uploadExecute(UploadFileDTO dto, MultipartFile file) {
        //准备上传文件参数
        UploadFile uploadFile = new UploadFile();
        uploadFile.setFileMd5(dto.getFileMd5());
        uploadFile.setBucketName(minioConfig.getBucketName());
        String extendName = fileUtils.getFileExtendName(dto.getFileName());
        String uploadPath = fileUtils.getOriginalFileStorePath(dto.getFileMd5(), extendName);
        uploadFile.setUploadPath(uploadPath);
        uploadFile.setCurrentChunkIndex(dto.getCurrentChunkIndex());
        uploadFile.setChunkNumber(dto.getChunkNumber());
        uploadFile.setCurrentChunkSize(dto.getCurrentChunkSize());
        uploadFile.setFileSize(dto.getFileSize());
        uploadFile.setIsChunk(true);
        try {
            //上传分片文件
            return uploader.uploadByChunkFile(uploadFile,file.getInputStream());
        } catch (IOException e) {
            log.error("分片文件上传失败", e);
            throw new CommonException(ErrorCode.SERVICE_ERROR);
        }
    }

    private FileBase uploadFileBasePersistence(UploadFileDTO dto, String contentType, String storePath) {
        FileBase fileBase = new FileBase();
        fileBase.setFileSize(dto.getFileSize());
        fileBase.setContentType(contentType);
        fileBase.setBucketName(minioConfig.getBucketName());
        fileBase.setObjectPath(storePath);
        fileBase.setFileMd5(dto.getFileMd5());
        fileBase.setRefCount(1);

        //保存到数据库
        fileBaseMapper.insert(fileBase);
        return fileBase;
    }

    private UserFile uploadUserFilePersistence(FileBase fileBase, UploadFileDTO dto) {
        if (fileBase == null || fileBase.getId() == null) {
            log.error("上传文件持久化阶段异常 ->");
            throw new CommonException(ErrorCode.SERVICE_ERROR.getCode(), "服务器内部异常，上传失败");
        }
        UserFile userFile = new UserFile();

        Long userId = UserContext.getUser();
        String fileExtendName = fileUtils.getFileExtendName(dto.getFileName());
        FileTypeEnum fileTypeEnum = getFileTypeEnum(fileBase.getContentType());

        userFile.setUserId(userId);
        userFile.setFileId(fileBase.getId());
        userFile.setParentId(dto.getParentFileId());
        userFile.setFileName(dto.getFileName());
        userFile.setFileSuffix(fileExtendName);
        userFile.setFileSize(dto.getFileSize());
        userFile.setFileMd5(dto.getFileMd5());
        userFile.setContentType(fileBase.getContentType());
        userFile.setFileType(fileTypeEnum);
        userFile.setDeleted(0L);
        //保存到数据库
        userFileMapper.insert(userFile);

        return userFile;
    }

    private void postProduction(FileTypeEnum fileTypeEnum, UploadFileDTO dto, String storePath) {
        try{
            //图片制作缩略图
            if (fileTypeEnum == IMAGE) {
                //获取该文件输入流
                DownloadFile downloadFile = new DownloadFile(minioConfig.getBucketName(), storePath);
                InputStream download = downloader.download(downloadFile);
                //制作缩略图
                byte[] thumbnailBytes = thumbnailUtils.createThumbnailBytes(download);
                //构建缩略图存储路径
                String thumbnailPath = fileUtils.getThumbnailFileStorePath(dto.getFileMd5(), fileUtils.getFileExtendName(dto.getFileName()));
                //上传缩略图
                UploadFile uploadFile = new UploadFile(thumbnailPath, (long) thumbnailBytes.length, minioConfig.getBucketName());
                uploader.uploadBySingleFile(uploadFile, new ByteArrayInputStream(thumbnailBytes));
            }
            //TODO处理视频关键帧
            if (fileTypeEnum == VIDEO) {
                log.error("暂时无法提取视频关键帧");
            }
        }catch (CommonException e){
            log.error("文件后期处理错误 -> ", e);
        }
    }


    @Override
    public DownloadInfoVO getDownloadInfo(Long userFileId) {
        Long userId = UserContext.getUser();
        UserFile userFile = userFileMapper.selectById(userFileId);
        if (userFile == null || !userFile.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.AUTHORITY_ERROR.getCode(), "暂无权限下载该文件");
        }
        if (userFile.getFileId() == null) {
            log.error("数据库UserFile中id为:{}的字段不完整 -> ", userFileId);
            throw new CommonException(ErrorCode.SERVICE_ERROR);
        }
        FileBase baseFile = fileBaseMapper.selectById(userFile.getFileId());
        return new DownloadInfoVO(baseFile.getObjectPath(), userId);
    }


    @Override
    public void downloadFile(HttpServletRequest request, HttpServletResponse response, DownloadFileDTO dto) {
        //获取文件下载地址
        String downloadPath = FileDownloadContext.getStorePath();
        Long userId = FileDownloadContext.getUserId();
        if (downloadPath == null || !userId.equals(UserContext.getUser())) {
            throw new ForbiddenException(ErrorCode.AUTHORITY_ERROR.getCode(), "请求资源无权限");
        }
        DownloadFile downloadFile;
        if (dto.getChunked()) {
            //分片下载
            Long[] range = HttpUtils.getRequestRange(request, dto.getFileSize());
            //设置下载相关请求头
            HttpUtils.setRangeDownloadResponse(response, dto.getFileName(), dto.getFileSize(), dto.getContentType(), range[0], range[1]);
            downloadFile = new DownloadFile(minioConfig.getBucketName(), downloadPath, range[0], range[1] - range[0] + 1);

        } else {
            //文件整体下载
            downloadFile = new DownloadFile(minioConfig.getBucketName(), downloadPath);
            HttpUtils.setDownloadResponse(response, dto.getFileName(), dto.getFileSize(), dto.getContentType());
        }
        //下载文件
        try (InputStream is = downloader.download(downloadFile);) {
            response.getOutputStream().write(is.readAllBytes());
        } catch (IOException e) {
            log.error("下载失败 -> ", e);
            throw new RuntimeException("文件下载失败");
        }
    }

    @Override
    public void previewFile(HttpServletRequest request, HttpServletResponse response, PreviewFileDTO dto) {
        Long userId = FileDownloadContext.getUserId();
        String storePath = FileDownloadContext.getStorePath();

        if (storePath == null || !userId.equals(UserContext.getUser())) {
            throw new ForbiddenException(ErrorCode.AUTHORITY_ERROR.getCode(), "请求资源无权限");
        }
        //判断是否分流
        DownloadFile downloadFile = null;
        if (dto.getChunkStreamed()) {
            Long[] range = HttpUtils.getRequestRange(request, dto.getFileSize());
            //计算结束位置
            HttpUtils.setRangePreviewResponse(response, range[0], range[1], dto.getFileSize(), dto.getContentType());
            downloadFile = new DownloadFile(minioConfig.getBucketName(), storePath, range[0], range[1] - range[0] + 1, dto.getChunkStreamed());
        } else {
            HttpUtils.setPreviewResponse(response, dto.getFileSize(), dto.getContentType());
            downloadFile = new DownloadFile(minioConfig.getBucketName(), storePath);
        }

        //下载文件
        try (InputStream is = downloader.download(downloadFile)) {
            byte[] buff = new byte[1024 * 1024 * 5];
            int len;
            while ((len = is.read(buff)) != -1) {
                response.getOutputStream().write(buff, 0, len);
            }
            log.debug("加载大小：{}", buff.length);
        } catch (Exception e) {
            if (e.getMessage().contains("Broken pipe")) {
                log.debug("链接断开");
            } else {
                log.error("文件预览失败 -> ", e);
                throw new CommonException(ErrorCode.SERVICE_ERROR);
            }
        }
    }

    @Override
    public PageResult<PreviewImagesVO> pagePreviewImages(PageParams params) {
        //获取登录用户
        Long userId = UserContext.getUser();
        //条件分页查询
        LambdaQueryWrapper<UserFile> pageWrapper = new LambdaQueryWrapper<UserFile>()
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getFileType, IMAGE)
                .eq(UserFile::getDeleted, 0);

        Page<UserFile> page = userFileMapper.selectPage(params.toPage(), pageWrapper);
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
        LambdaQueryWrapper<FileBase> listWrapper = new LambdaQueryWrapper<FileBase>().in(FileBase::getId, fileIds);
        Map<Long, FileBase> fileBaseMap = fileBaseMapper.selectList(listWrapper)
                .stream()
                .collect(Collectors.toMap(FileBase::getId, fileBase -> fileBase));
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
            FileBase fileBase = fileBaseMap.get(record.getFileId());
            String extendName = fileUtils.getFileExtendName(record.getFileName());
            //获取临时签名url
            String[] urls = getTempSignedUrl(fileBase.getFileMd5(), extendName, minioConfig.getTempSignedUrlExpireMinute());
            vo.setThumbUrl(urls[1]);
            vo.setOriginalUrl(urls[0]);
            vo.setYear(year);
            vo.setMonth(month);
            vo.setDay(day);
            res.add(vo);
        }
        return new PageResult<>(page.getTotal(), page.getSize(), page.getCurrent(), res);
    }

    /**
     * 判断当前目录是否存在相同文件名文件
     */
    private UserFile fileNameExist(Long userId, Long parentId, String fileName) {
        LambdaQueryWrapper<UserFile> wrapper = new LambdaQueryWrapper<UserFile>()
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getParentId, parentId)
                .eq(UserFile::getFileName, fileName)
                .eq(UserFile::getDeleted, 0);

        return userFileMapper.selectOne(wrapper);
    }

    /**
     * 获取临时预览路径
     *
     * @param fileMd5      文件md5值
     * @param extendName   文件扩展名
     * @param expireMinute url过期时间
     * @return [源文件url, 缩略图url]
     */
    private String[] getTempSignedUrl(String fileMd5, String extendName, Integer expireMinute) {
        String[] res = new String[2];
        //分别获取路径
        String originalPath = fileUtils.getOriginalFileStorePath(fileMd5, extendName);
        String thumbnailPath = fileUtils.getThumbnailFileStorePath(fileMd5, extendName);
        //获取url
        res[0] = minioUtils.getTempSignedUrl(originalPath, expireMinute);
        res[1] = minioUtils.getTempSignedUrl(thumbnailPath, expireMinute);

        return res;
    }
}
