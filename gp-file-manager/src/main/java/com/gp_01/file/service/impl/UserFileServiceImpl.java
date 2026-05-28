package com.gp_01.file.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gp_01.common.context.UserContext;
import com.gp_01.common.domain.Result;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.common.enums.FileTypeEnum;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.common.exception.ForbiddenException;
import com.gp_01.common.utils.FileTypeResolver;
import com.gp_01.common.utils.TimeUtils;
import com.gp_01.file.config.FileManagerServiceProperties;
import com.gp_01.file.config.MinioConfig;
import com.gp_01.file.domain.po.FileBase;
import com.gp_01.file.domain.po.UserFile;
import com.gp_01.file.domain.query.PageFilesQuery;
import com.gp_01.file.domain.vo.ListRecycleBinVO;
import com.gp_01.file.domain.vo.PreviewImagesVO;
import com.gp_01.file.mapper.UserFileMapper;
import com.gp_01.file.service.IFileBaseService;
import com.gp_01.file.service.IUserFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gp_01.file.util.FileThumbnailUtils;
import com.gp_01.file.util.MinioUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.gp_01.common.enums.FileTypeEnum.DIRECTORY;
import static com.gp_01.common.enums.FileTypeEnum.IMAGE;

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

    @Override
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
        if(fileBase.getContentType().split("/")[0].equals("image")){
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
        if (exist){
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
        if(records.isEmpty()){
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
        if(cnt != 0){
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
