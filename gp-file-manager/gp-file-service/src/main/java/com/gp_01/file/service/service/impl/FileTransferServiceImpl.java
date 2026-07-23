package com.gp_01.file.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gp_01.common.context.UserContext;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.enums.FileTypeEnum;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.file.model.domain.cache.redis.UploadFileCache;
import com.gp_01.file.model.domain.dto.PreviewFileDTO;
import com.gp_01.file.model.domain.dto.UploadAuthorizationDTO;
import com.gp_01.file.model.domain.po.FileObject;
import com.gp_01.file.model.domain.po.UserFile;
import com.gp_01.file.model.domain.vo.PreviewImagesVO;
import com.gp_01.file.model.domain.vo.UploadFileVO;
import com.gp_01.file.service.config.MinioConfig;
import com.gp_01.file.service.mapper.FileObjectMapper;
import com.gp_01.file.service.mapper.UserFileMapper;
import com.gp_01.file.service.operation.download.Downloader;
import com.gp_01.file.service.operation.download.domain.DownloadFile;
import com.gp_01.file.service.operation.preview.product.MinioPreviewer;
import com.gp_01.file.service.operation.upload.product.MinioUploader;
import com.gp_01.file.service.service.IFileTransferService;
import com.gp_01.file.service.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileTransferServiceImpl implements IFileTransferService {

    private final Downloader downloader;

    private final MinioPreviewer minioPreviewer;

    private final MinioUploader minioUploader;

    private final RedisUtils redisUtils;

    private final FileUtils fileUtils;

    private final UserFileMapper userFileMapper;

    private final FileObjectMapper fileObjectMapper;

    private final MinioConfig minioConfig;

    private final MinioUtils minioUtils;

    private final ThumbnailUtils thumbnailUtils;

    @Override
    public String directionConnectionDownload(Long id) {
        Long userId = UserContext.getUser();
        //查数据库获取文件信息
        LambdaQueryWrapper<UserFile> selectOneWrapper = new LambdaQueryWrapper<UserFile>()
                .eq(UserFile::getId, id)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getDeleted, 0);
        UserFile userFile = userFileMapper.selectOne(selectOneWrapper);
        if (userFile == null) {
            throw new BadRequestException(ErrorCode.RECOURSE_NOT_FOUND_ERROR.getCode(), "资源不存在");
        }
        //查数据库获取下载路径
        FileObject fileBase = fileObjectMapper.selectById(userFile.getObjectId());
        if (fileBase == null) {
            log.error("数据库不一致user_file.file_id = {}, file_base = null", userFile.getObjectId());
            throw new BadRequestException(ErrorCode.RECOURSE_NOT_FOUND_ERROR.getCode(), "资源不存在");
        }

        String contentType = fileBase.getContentType();
        String fileName = userFile.getFileName();
        String downloadPath = fileBase.getObjectPath();

        //获取预签名url
        DownloadFile downloadFile = new DownloadFile(minioConfig.getBucketName(), contentType, fileName, downloadPath, 5, TimeUnit.MINUTES);
        return downloader.downloadByIssuePreSignedUrl(downloadFile);
    }

    @Override
    public UploadFileVO uploadAuthorize(UploadAuthorizationDTO dto) {
        Long userId = UserContext.getUser();

        //判断该目录是否有重复文件
        UserFile userFile = fileNameExist(userId, dto.getParentId(), dto.getFileName());
        if (userFile != null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "该目录下存在同名文件");
        }
        UploadFileVO vo = new UploadFileVO();
        //判断秒传
        FileObject fileObject = fileObjectMapper.selectOne(new LambdaQueryWrapper<FileObject>().eq(FileObject::getFileMd5, dto.getFileMd5()));
        if (fileObject != null) {
            vo.setIsUpload(true);

            //引用+1
            fileObjectMapper.incrementRefCount(fileObject.getFileMd5());

            //存数据库
            userFile = new UserFile();
            userFile.setUserId(userId);
            userFile.setParentId(dto.getParentId());
            userFile.setObjectId(fileObject.getId());
            userFile.setFileName(dto.getFileName());
            userFile.setIsDirectory(0);
            FileTypeEnum mediaCategory = FileTypeEnum.getFileTypeEnum(fileObject.getContentType());
            userFile.setMediaCategory(mediaCategory);
            userFile.setSort(0);
            userFile.setDeleted(0L);
            userFileMapper.insert(userFile);
            return vo;
        }
        String uploadId;
        String objectPath = fileUtils.getOriginalFileStorePath(dto.getFileMd5(), dto.getFileName());
        if (dto.getIsSlice()) {
            //获取uploadId
            uploadId = minioUploader.getUploadId(minioConfig.getBucketName(), objectPath);
        } else {
            uploadId = UUID.randomUUID().toString();
        }
        vo.setIsUpload(false);
        vo.setUploadId(uploadId);

        //存缓存
        String key = "gb_01:file-service:upload-file:" + userId + ":" + uploadId;
        UploadFileCache cache = new UploadFileCache();
        cache.setBucketName(minioConfig.getBucketName());
        cache.setObjectPath(objectPath);
        cache.setFileMd5(dto.getFileMd5());
        cache.setFileName(dto.getFileName());
        cache.setFileSize(dto.getFileSize());
        cache.setParentId(dto.getParentId());
        redisUtils.setObject(key, cache, 10L, TimeUnit.MINUTES);

        return vo;
    }

    @Override
    public Map<Integer, String> directConnectionChunkUploadFile(String uploadId, List<Integer> chunkNumbers) {
        Long userId = UserContext.getUser();
        String key = "gb_01:file-service:upload-file:" + userId + ":" + uploadId;
        UploadFileCache cache = redisUtils.getObject(key, UploadFileCache.class);
        if (cache == null) {
            log.error("在文件上传阶段未找到上传任务id -> key: {}", key);
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "上传任务未找到");
        }
        return minioUploader.getChunkUploadUrls(cache.getBucketName(), cache.getObjectPath(), uploadId, chunkNumbers, 10, TimeUnit.MINUTES);

    }

    @Override
    public String directConnectionWholeUploadFile(String uploadId) {
        Long userId = UserContext.getUser();
        String key = "gb_01:file-service:upload-file:" + userId + ":" + uploadId;

        UploadFileCache cache = redisUtils.getObject(key, UploadFileCache.class);
        if (cache == null) {
            log.error("在文件上传阶段未找到上传任务id -> key: {}", key);
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "上传任务未找到");
        }
        //获取上传url
        return minioUploader.getWholeUploadUrl(cache.getBucketName(), cache.getObjectPath(), 10, TimeUnit.MINUTES);
    }

    @Override
    public String directionConnectionPreview(PreviewFileDTO dto) {
        Long userId = UserContext.getUser();
        LambdaQueryWrapper<UserFile> previewWrapper = new LambdaQueryWrapper<UserFile>().eq(UserFile::getId, dto.getUserFileId()).eq(UserFile::getUserId, userId)
                .eq(UserFile::getDeleted, 0);
        UserFile userFile = userFileMapper.selectOne(previewWrapper);
        if (userFile == null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "数据不存在");
        }
        FileObject fileObject = fileObjectMapper.selectById(userFile.getObjectId());
        if (fileObject == null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "数据不存在");
        }

        return minioPreviewer.getPreviewPreSignedUrl(fileObject.getBucketName(), fileObject.getObjectPath(), fileObject.getContentType(), 10, TimeUnit.MINUTES);


    }

    @Override
    public PageResult<PreviewImagesVO> previewThumbnailsPage(PageParams params) {
        Long userId = UserContext.getUser();
        LambdaQueryWrapper<UserFile> pageWrapper = new LambdaQueryWrapper<UserFile>()
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getDeleted, 0)
                .eq(UserFile::getMediaCategory, FileTypeEnum.IMAGE)
                .orderByDesc(UserFile::getCreateTime);
        Page<UserFile> page = userFileMapper.selectPage(params.toPage(), pageWrapper);
        List<UserFile> records = page.getRecords();
        //判空
        if(records == null || records.isEmpty()){
            return PageResult.empty();
        }
        //收集fileIds
        List<Long> fileIds = records.stream().map(UserFile::getObjectId).toList();
        //查所有文件物理信息， 映射 fileId -> fileObject
        Map<Long, FileObject> fileObjectMap = fileObjectMapper.selectByIds(fileIds).stream().collect(Collectors.toMap(FileObject::getId, fileObject -> fileObject));
        //结果集
        List<PreviewImagesVO> res = new ArrayList<>();
        //构建每条数据
        for (UserFile record : records) {
            PreviewImagesVO vo = new PreviewImagesVO();
            Long fileId = record.getObjectId();
            FileObject fileObject = fileObjectMap.get(fileId);
            //获取缩略图签名
            String thumbnailUrl = minioPreviewer.getPreviewPreSignedUrl(minioConfig.getBucketName(), fileObject.getObjectPath(), fileObject.getContentType(), 10, TimeUnit.MINUTES);
            vo.setFileId(fileId);
            vo.setFileName(record.getFileName());
            vo.setFileSize(fileObject.getFileSize());
            vo.setThumbUrl(thumbnailUrl);
            vo.setCreateTime(record.getCreateTime());
            res.add(vo);
        }

        return new PageResult<>(page.getTotal(), page.getSize(), page.getCurrent(), res);
    }

    @Override
    public void saveUploadFile(String uploadId) {
        Long userId = UserContext.getUser();
        String key = "gb_01:file-service:upload-file:" + userId + ":" + uploadId;
        UploadFileCache cache = redisUtils.getDelObject(key, UploadFileCache.class);

        if (cache == null) {
            log.error("在合并文件阶段未找到上传任务id -> key: {}", key);
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "上传任务未找到");
        }

        //写物理文件数据库
        String contentType = fileUtils.getContentTypeByFileBinary(cache.getObjectPath(), cache.getFileName());
        String fielETag = minioUtils.getFielETag(cache.getBucketName(), cache.getObjectPath());
        FileObject fileObject = new FileObject();
        fileObject.setBucketName(cache.getBucketName());
        fileObject.setObjectPath(cache.getObjectPath());
        fileObject.setFileMd5(cache.getFileMd5());
        fileObject.setETag(fielETag);
        fileObject.setFileSize(cache.getFileSize());
        fileObject.setContentType(contentType);
        fileObject.setFileSuffix(".test");
        fileObject.setRefCount(1);
        fileObject.setUploadUserId(userId);
        fileObject.setIsDeleted(0L);
        fileObjectMapper.insert(fileObject);

        //写逻辑文件数据库
        FileTypeEnum mediaCategory = FileTypeEnum.getFileTypeEnum(contentType);
        UserFile userFile = new UserFile();
        userFile.setUserId(userId);
        userFile.setParentId(cache.getParentId());
        userFile.setObjectId(fileObject.getId());
        userFile.setFileName(cache.getFileName());
        userFile.setFileSize(cache.getFileSize());
        userFile.setIsDirectory(0);
        userFile.setMediaCategory(mediaCategory);
        userFile.setSort(0);
        userFile.setDeleted(0L);
        userFileMapper.insert(userFile);
    }

    @Override
    public void uploadChunkFileMerge(String uploadId, Map<Integer, String> parts) {
        Long userId = UserContext.getUser();
        String key = "gb_01:file-service:upload-file:" + userId + ":" + uploadId;
        UploadFileCache cache = redisUtils.getDelObject(key, UploadFileCache.class);

        if (cache == null) {
            log.error("在合并文件阶段未找到上传任务id -> key: {}", key);
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "上传任务未找到");
        }
        //合并分片，获取eTag
        String eTag = minioUploader.chunkFileMerge(cache.getBucketName(), cache.getObjectPath(), uploadId, parts);

        //TODO 删除数据库中分页进度


        //写物理文件数据库
        String contentType = fileUtils.getContentTypeByFileBinary(cache.getObjectPath(), cache.getFileName());

        FileObject fileObject = new FileObject();
        fileObject.setBucketName(cache.getBucketName());
        fileObject.setObjectPath(cache.getObjectPath());
        fileObject.setFileMd5(cache.getFileMd5());
        fileObject.setETag(eTag);
        fileObject.setFileSize(cache.getFileSize());
        fileObject.setContentType(contentType);
        fileObject.setFileSuffix("");
        fileObject.setRefCount(1);
        fileObject.setUploadUserId(userId);
        fileObject.setIsDeleted(0L);
        fileObjectMapper.insert(fileObject);

        //写逻辑文件数据库
        FileTypeEnum mediaCategory = FileTypeEnum.getFileTypeEnum(contentType);
        UserFile userFile = new UserFile();
        userFile.setUserId(userId);
        userFile.setParentId(cache.getParentId());
        userFile.setObjectId(fileObject.getId());
        userFile.setFileName(cache.getFileName());
        userFile.setFileSize(cache.getFileSize());
        userFile.setIsDirectory(0);
        userFile.setMediaCategory(mediaCategory);
        userFile.setSort(0);
        userFile.setDeleted(0L);
        userFileMapper.insert(userFile);
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
