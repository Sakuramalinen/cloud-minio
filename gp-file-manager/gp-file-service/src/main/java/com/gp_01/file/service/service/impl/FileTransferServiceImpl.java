package com.gp_01.file.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gp_01.common.context.UserContext;
import com.gp_01.common.domain.Result;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.enums.FileTypeEnum;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.common.exception.CommonException;
import com.gp_01.file.model.domain.cache.redis.UploadFileCache;
import com.gp_01.file.model.domain.dto.PreviewFileDTO;
import com.gp_01.file.model.domain.dto.UploadAuthorizationDTO;
import com.gp_01.file.model.domain.dto.UploadFilePostHandleDTO;
import com.gp_01.file.model.domain.po.FileObject;
import com.gp_01.file.model.domain.po.UploadTaskRecord;
import com.gp_01.file.model.domain.po.UserFile;
import com.gp_01.file.model.domain.vo.PreviewImagesVO;
import com.gp_01.file.model.domain.vo.UploadFileVO;
import com.gp_01.file.service.config.FileServiceProperties;
import com.gp_01.file.service.config.MinioConfig;
import com.gp_01.file.service.constants.RabbitmqFileConstants;
import com.gp_01.file.service.constants.RedisKeyFormatter;
import com.gp_01.file.service.mapper.FileObjectMapper;
import com.gp_01.file.service.mapper.UploadTaskRecordMapper;
import com.gp_01.file.service.mapper.UserFileMapper;
import com.gp_01.file.service.operation.download.Downloader;
import com.gp_01.file.service.operation.download.domain.DownloadFile;
import com.gp_01.file.service.operation.preview.product.MinioPreviewer;
import com.gp_01.file.service.operation.upload.product.MinioUploader;
import com.gp_01.file.service.service.IFileTransferService;
import com.gp_01.file.service.util.*;
import com.gp_01.user.api.client.UserClient;
import com.gp_01.user.model.domain.dto.UpdateUsedStoreSizeDTO;
import com.gp_01.user.model.domain.po.User;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileTransferServiceImpl implements IFileTransferService {

    private final FileServiceProperties fileServiceProperties;

    private final Downloader downloader;

    private final MinioPreviewer minioPreviewer;

    private final MinioUploader minioUploader;

    private final RedisUtils redisUtils;

    private final FileUtils fileUtils;

    private final UserFileMapper userFileMapper;

    private final FileObjectMapper fileObjectMapper;

    private final UploadTaskRecordMapper uploadTaskRecordMapper;

    private final MinioConfig minioConfig;

    private final MinioUtils minioUtils;

    private final UserClient userClient;

    private final ThumbnailUtils thumbnailUtils;

    private final RabbitTemplate rabbitTemplate;

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
    @Transactional(rollbackFor = Exception.class)
    public UploadFileVO uploadAuthorize(UploadAuthorizationDTO dto) {
        Long userId = UserContext.getUser();

        //判断目录是否存在
        LambdaQueryWrapper<UserFile> parentIdExistWrapper = new LambdaQueryWrapper<UserFile>()
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getId, dto.getParentId())
                .eq(UserFile::getDeleted, 0);
        UserFile parentIdExist = userFileMapper.selectOne(parentIdExistWrapper);
        if (parentIdExist == null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "该目录不存在");
        }
        //判断该目录是否有重复文件
        UserFile userFile = fileNameExist(userId, dto.getParentId(), dto.getFileName());
        if (userFile != null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "该目录下存在同名文件");
        }
        //判断剩余空间是否足够
        Result<User> userResult = userClient.getUserInfo(userId);
        User userinfo = userResult.getData();
        if(userinfo.getTotalStoreSize() - userinfo.getUsedStoreSize() < dto.getFileSize()){
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "可用存储空间不足");
        }

        //判断秒传
        FileObject fileObject = fileObjectMapper.selectOne(new LambdaQueryWrapper<FileObject>().eq(FileObject::getFileMd5, dto.getFileMd5()));
        if (fileObject != null) {
            //引用+1
            fileObjectMapper.incrementRefCount(fileObject.getFileMd5());

            FileTypeEnum mediaCategory = FileTypeEnum.getFileTypeEnum(fileObject.getContentType());
            //存数据库
            userFile = new UserFile()
                    .setUserId(userId)
                    .setParentId(dto.getParentId())
                    .setObjectId(fileObject.getId())
                    .setFileName(dto.getFileName())
                    .setFileSize(dto.getFileSize())
                    .setIsDirectory(0)
                    .setMediaCategory(mediaCategory)
                    .setSort(0)
                    .setDeleted(0L);
            userFileMapper.insert(userFile);
            //TODO 增加使用空间
            return new UploadFileVO()
                    .setIsUpload(true);

        }


        //判断是否是以前暂停上传的任务
        LambdaQueryWrapper<UploadTaskRecord> wrapper = new LambdaQueryWrapper<UploadTaskRecord>()
                .eq(UploadTaskRecord::getFileMd5, dto.getFileMd5())
                .eq(UploadTaskRecord::getParentId, dto.getParentId())
                .eq(UploadTaskRecord::getUserId, userId);
        UploadTaskRecord uploadTaskRecord = uploadTaskRecordMapper.selectOne(wrapper);
        if (uploadTaskRecord == null) {
            //判断是否需要分片
            boolean isChunked = false;
            long chunkSize = -1;
            StringBuilder bitmap = new StringBuilder();
            if (dto.getFileSize() >= fileServiceProperties.getChunkUploadThreshold().toBytes()) {
                //计算分片大小
                chunkSize = calculateChunkSize(dto.getFileSize());
                if (chunkSize != -1) {
                    isChunked = true;
                    int totalChunks = (int) ((dto.getFileSize() + chunkSize - 1) / chunkSize);
                    bitmap.append("0".repeat(Math.max(0, totalChunks)));
                }
            }

            String uploadId;
            String objectPath = fileUtils.getOriginalFileStorePath(dto.getFileMd5(), dto.getFileName());
            if (isChunked) {
                //获取uploadId
                uploadId = minioUploader.getUploadId(minioConfig.getBucketName(), objectPath);
            } else {
                uploadId = UUID.randomUUID().toString();
            }
            LocalDateTime expiryTime = LocalDateTime.now().plusDays(7);
            //生成上传任务对象
            uploadTaskRecord = new UploadTaskRecord()
                    .setParentId(dto.getParentId())
                    .setFileMd5(dto.getFileMd5())
                    .setFileName(dto.getFileName())
                    .setUploadId(uploadId)
                    .setUserId(userId)
                    .setStatus(0)
                    .setUploadType(isChunked ? 1 : 0)
                    .setChunkSize(chunkSize)
                    .setFileSize(dto.getFileSize())
                    .setBucketName(minioConfig.getBucketName())
                    .setObjectPath(objectPath)
                    .setChunkBitmap(bitmap.toString())
                    .setExpireTime(expiryTime);
            uploadTaskRecordMapper.insert(uploadTaskRecord);
        }

        //存缓存
        String key = RedisKeyFormatter.fileUploadInfoKey(userId, uploadTaskRecord.getTaskId());
        UploadFileCache cache = new UploadFileCache()
                .setBucketName(uploadTaskRecord.getBucketName())
                .setObjectPath(uploadTaskRecord.getObjectPath())
                .setUploadId(uploadTaskRecord.getUploadId());
        redisUtils.setObject(key, cache, 10L, TimeUnit.MINUTES);

        return new UploadFileVO()
                .setIsUpload(false)
                .setTaskId(uploadTaskRecord.getTaskId())
                .setChunkSize(uploadTaskRecord.getChunkSize())
                .setIsChunked(uploadTaskRecord.getUploadType() == 1)
                .setChunkBitmap(uploadTaskRecord.getChunkBitmap());

    }


    @Override
    public Map<Integer, String> directConnectionChunkUploadFile(Long taskId, List<Integer> chunkNumbers) {
        Long userId = UserContext.getUser();
        String key = RedisKeyFormatter.fileUploadInfoKey(userId, taskId);
        //获取缓存并续期10分钟
        UploadFileCache cache = redisUtils.getObjectAndReNew(key, UploadFileCache.class, Duration.ofMinutes(10));
        if (cache == null) {
            throw new BadRequestException(ErrorCode.AUTHORITY_EXPIRATION_ERROR.getCode(), "上传key过期，请重新获取");
        }
        return minioUploader.getChunkUploadUrls(cache.getBucketName(), cache.getObjectPath(), cache.getUploadId(), chunkNumbers, 10, TimeUnit.MINUTES);

    }

    @Override
    public String directConnectionWholeUploadFile(Long taskId) {
        Long userId = UserContext.getUser();
        String key = RedisKeyFormatter.fileUploadInfoKey(userId, taskId);
        //获取缓存并续期10分钟
        UploadFileCache cache = redisUtils.getObjectAndReNew(key, UploadFileCache.class, Duration.ofMinutes(10));
        if (cache == null) {
            throw new BadRequestException(ErrorCode.AUTHORITY_EXPIRATION_ERROR.getCode(), "上传key过期，请重新获取");
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
        if (records == null || records.isEmpty()) {
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
    @Transactional(rollbackFor = Exception.class)
    public void saveUploadFile(Long taskId) {
        Long userId = UserContext.getUser();

        //检查任务是否存在
        UploadTaskRecord uploadTaskRecord = uploadTaskRecordMapper.selectById(taskId);
        if (uploadTaskRecord == null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "任务不存在");
        }
        //检查文件是否上传成功
        String eTag = minioUtils.getFielETag(uploadTaskRecord.getBucketName(), uploadTaskRecord.getObjectPath());
        if (eTag.isEmpty()) {
            //标记上传任务为失败
            LambdaUpdateWrapper<UploadTaskRecord> updateWrapper = new LambdaUpdateWrapper<UploadTaskRecord>()
                    .eq(UploadTaskRecord::getTaskId, taskId)
                    .set(UploadTaskRecord::getStatus, 3);
            uploadTaskRecordMapper.update(updateWrapper);
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "上传文件失败");
        }

        String contentType = fileUtils.getContentTypeByFileBinary(uploadTaskRecord.getObjectPath(), uploadTaskRecord.getFileName());
        FileTypeEnum mediaCategory = FileTypeEnum.getFileTypeEnum(contentType);

        //写物理文件数据库
        //写逻辑文件数据库
        saveFile2DataBase(uploadTaskRecord, eTag, userId, contentType, mediaCategory);

        //删除数据库中上传任务
        uploadTaskRecordMapper.deleteById(taskId);

        //累加空间使用大小
        userClient.incrementUsedStoreSize(new UpdateUsedStoreSizeDTO(uploadTaskRecord.getFileSize()));



        //文件后期处理
        String fileExtendName = fileUtils.getFileExtendName(uploadTaskRecord.getFileName());
        String thumbnailFileStorePath = fileUtils.getThumbnailFileStorePath(uploadTaskRecord.getFileMd5(), fileExtendName);
        UploadFilePostHandleDTO dto = new UploadFilePostHandleDTO(minioConfig.getBucketName(), uploadTaskRecord.getObjectPath(), thumbnailFileStorePath, contentType);
        rabbitTemplate.convertAndSend(RabbitmqFileConstants.EXCHANGE_TOPIC_FILE, RabbitmqFileConstants.RK_UPLOAD_POST_PROCESS, dto);

    }

    @Override
    public void uploadFilePostHandle(UploadFilePostHandleDTO dto) {

        boolean isImage = dto.getContentType().split("/")[0].equals("image");
        //TODO制作图片缩略图
        if(isImage){
            DownloadFile downloadFile = new DownloadFile(dto.getBucketName(), dto.getDownloadObjectPath());
            InputStream inputStream = downloader.downloadBySingleFile(downloadFile);
            byte[] thumbnailBytes = thumbnailUtils.createThumbnailBytes(inputStream);
            try {
                minioUploader.uploadFileWhole(thumbnailBytes, dto.getBucketName(), dto.getUploadObjectPath(), dto.getContentType());
            } catch (MinioException e) {
                log.error("缩略图上传失败");
                throw new CommonException(ErrorCode.OSS_ERROR.getCode(),"上传失败");
            }
        }
        //TODO提取视频封面

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadChunkFileMerge(Long taskId) {
        Long userId = UserContext.getUser();

        //检查任务是否存在
        UploadTaskRecord uploadTaskRecord = uploadTaskRecordMapper.selectById(taskId);
        if (uploadTaskRecord == null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "任务不存在");
        }
        String eTag = "";
        try {
            //合并分片，获取eTag
            minioUploader.chunkFileMerge(uploadTaskRecord.getBucketName(), uploadTaskRecord.getObjectPath(), uploadTaskRecord.getUploadId());
            //查询存储服务，检查是否合并成功
            eTag = minioUtils.getFielETag(uploadTaskRecord.getBucketName(), uploadTaskRecord.getObjectPath());

        } catch(Exception e){
            //标记上传任务为失败
            LambdaUpdateWrapper<UploadTaskRecord> updateWrapper = new LambdaUpdateWrapper<UploadTaskRecord>()
                    .eq(UploadTaskRecord::getTaskId, taskId)
                    .set(UploadTaskRecord::getStatus, 3);
            uploadTaskRecordMapper.update(updateWrapper);
        } finally {
            //清除所有切片
            minioUtils.abortInCompleteMultipartUpload(uploadTaskRecord.getBucketName(), uploadTaskRecord.getObjectPath(), uploadTaskRecord.getUploadId());
        }
        if (eTag.isEmpty()) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "上传文件失败");
        }

        //获取contentType
        String contentType = fileUtils.getContentTypeByFileBinary(uploadTaskRecord.getObjectPath(), uploadTaskRecord.getFileName());
        //获取文件类型
        FileTypeEnum mediaCategory = FileTypeEnum.getFileTypeEnum(contentType);

        //存数据库
        saveFile2DataBase(uploadTaskRecord, eTag, userId, contentType, mediaCategory);

        //删除数据库中上传任务
        uploadTaskRecordMapper.deleteById(taskId);

        //累加已使用空间
        userClient.incrementUsedStoreSize(new UpdateUsedStoreSizeDTO(uploadTaskRecord.getFileSize()));

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

    /**
     * 计算该文件每个分片大小
     *
     * @param fileSize 文件总大小
     * @return 每个分片大小 执行错误返回 -1
     */
    private long calculateChunkSize(Long fileSize) {
        Map<DataSize, DataSize> chunkStrategyMap = fileServiceProperties.getChunkStrategyMap();
        if (chunkStrategyMap == null || chunkStrategyMap.isEmpty()) {
            log.error("gp:file-service:chunk-strategy-map配置读取失败");
            return -1;
        }
        TreeMap<DataSize, DataSize> treeMap = new TreeMap<>(chunkStrategyMap);

        Map.Entry<DataSize, DataSize> entry = treeMap.floorEntry(DataSize.ofBytes(fileSize));
        if (entry == null) {
            return -1;
        }
        return entry.getValue().toBytes();
    }

    /**
     * 上传文件后写文件逻辑表和文件物理表
     */
    private void saveFile2DataBase(UploadTaskRecord uploadTaskRecord, String eTag, Long userId, String contentType, FileTypeEnum mediaCategory) {
        //写物理文件数据库
        FileObject fileObject = new FileObject()
                .setBucketName(uploadTaskRecord.getBucketName())
                .setObjectPath(uploadTaskRecord.getObjectPath())
                .setFileMd5(uploadTaskRecord.getFileMd5())
                .setETag(eTag)
                .setFileSize(uploadTaskRecord.getFileSize())
                .setContentType(contentType)
                .setRefCount(1)
                .setUploadUserId(userId)
                .setIsDeleted(0L);
        fileObjectMapper.insert(fileObject);

        //写逻辑文件数据库
        UserFile userFile = new UserFile()
                .setUserId(userId)
                .setParentId(uploadTaskRecord.getParentId())
                .setObjectId(fileObject.getId())
                .setFileName(uploadTaskRecord.getFileName())
                .setFileSize(uploadTaskRecord.getFileSize())
                .setIsDirectory(0)
                .setMediaCategory(mediaCategory)
                .setSort(0)
                .setDeleted(0L);
        userFileMapper.insert(userFile);
    }
}
