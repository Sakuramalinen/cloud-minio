package com.gp_01.file.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gp_01.common.context.UploadInfoContext;
import com.gp_01.common.context.UserContext;
import com.gp_01.common.domain.Result;
import com.gp_01.common.domain.context.UploadInfo;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.enums.FileTypeEnum;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.common.exception.CommonException;
import com.gp_01.file.model.domain.dto.*;
import com.gp_01.file.model.domain.po.FileObject;
import com.gp_01.file.model.domain.po.UserFile;
import com.gp_01.file.model.domain.vo.PreviewImagesVO;
import com.gp_01.file.model.domain.vo.UploadFileVO;
import com.gp_01.file.model.domain.vo.UploadPreSignVO;
import com.gp_01.file.service.config.FileServiceProperties;
import com.gp_01.file.service.constants.RabbitmqFileConstants;
import com.gp_01.file.service.constants.RedisKeyFormatter;
import com.gp_01.file.service.mapper.FileObjectMapper;
import com.gp_01.file.service.mapper.UserFileMapper;
import com.gp_01.file.service.oss.FileManipulator;
import com.gp_01.file.service.oss.OSS;
import com.gp_01.file.service.service.IFileTransferService;
import com.gp_01.file.service.util.*;
import com.gp_01.user.api.client.UserClient;
import com.gp_01.user.model.domain.dto.UpdateUsedStoreSizeDTO;
import com.gp_01.user.model.domain.po.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.Part;

import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileTransferServiceImpl implements IFileTransferService {


    private final FileManipulator fileManipulator;

    private final OSS oss;

    private final RedisUtils redisUtils;

    private final FileUtils fileUtils;

    private final UserFileMapper userFileMapper;

    private final FileObjectMapper fileObjectMapper;

    private final UserClient userClient;

    private final ThumbnailUtils thumbnailUtils;

    private final RabbitTemplate rabbitTemplate;


    private final FileServiceProperties fileServiceProperties;


    @Override
    public FileObject instantUpdate(String fileMd5) {

        return fileObjectMapper.selectOne(new LambdaQueryWrapper<FileObject>().eq(FileObject::getFileMd5, fileMd5));
    }

    @Override
    public String getChunkUploadProgress() {
        Long userId = UserContext.getUser();
        UploadInfo uploadInfo = UploadInfoContext.getUploadInfo();
        if (uploadInfo == null) {
            throw new BadRequestException(ErrorCode.AUTHORITY_ERROR.getCode(), "暂无上传权限");
        }
        return uploadInfo.getBitmap();
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

        //判断剩余空间是否足够
        Result<User> userResult = userClient.getUserInfo(userId);
        User userinfo = userResult.getData();
        if (userinfo.getTotalStoreSize() - userinfo.getUsedStoreSize() < dto.getFileSize()) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "可用存储空间不足");
        }

        long chunkSize = 0;
        boolean isChunked = false;
        long chunkTotal = 0;

        //计算分片大小
        if (dto.getFileSize() >= fileServiceProperties.getChunkUploadThreshold().toBytes()) {
            chunkSize = calculateChunkSize(dto.getFileSize());
            if (chunkSize != -1) {
                isChunked = true;
                chunkTotal = (dto.getFileSize() + chunkSize - 1) / chunkSize;
            }
        }

        //获取对象存储路径
        UUID uuid = UUID.randomUUID();
        String objectPath = fileUtils.getObjectStorePath(uuid.toString(), dto.getFileName());

        String uploadId = uuid.toString();

        String chunkUploadId = null;
        StringBuilder sb = new StringBuilder();
        //获取分片上传id
        if (isChunked) {
            chunkUploadId = fileManipulator.initiateMultipartUpload(oss.getDefaultBucket(), objectPath, dto.getContentType());
            for (int i = 0; i < chunkTotal; i++) {
                sb.append("0");
            }
        }

        String bitmap = sb.toString();

        //创建缓存信息
        UploadInfo uploadInfo = new UploadInfo()
                .setUploadId(uploadId)
                .setChunkUploadId(chunkUploadId)
                .setObjectPath(objectPath)
                .setFileSize(dto.getFileSize())
                .setFileName(dto.getFileName())
                .setParentId(dto.getParentId())
                .setBitmap(bitmap)
                .setContentType(dto.getContentType());

        String key = RedisKeyFormatter.fileUploadInfoKey(userId, uploadId);
        redisUtils.setObject(key, uploadInfo, 1, TimeUnit.DAYS);

        return new UploadFileVO()
                .setUploadId(uploadId)
                .setIsChunked(isChunked)
                .setChunkSize(chunkSize)
                .setTotalChunk(chunkTotal);

    }


    @Override
    public UploadPreSignVO getUploadPreSignedUrl(UploadPreSignDTO dto) {
        Boolean isChunk = dto.getIsChunked();

        UploadInfo uploadInfo = UploadInfoContext.getUploadInfo();
        //判断是否分片
        if (isChunk) {
            HashMap<Integer, String> map = new HashMap<>();
            for (Integer chunkNumber : dto.getChunkNumbers()) {
                String url = fileManipulator.generateUploadPartPreSignedUrl(oss.getDefaultBucket(), uploadInfo.getObjectPath(), uploadInfo.getChunkUploadId(), chunkNumber, Duration.ofHours(1));
                map.put(chunkNumber, url);
            }
            return new UploadPreSignVO(map);
        } else {
            String preSignUrl = fileManipulator.generateUploadPreSignedUrl(oss.getDefaultBucket(), uploadInfo.getObjectPath(), uploadInfo.getContentType(), uploadInfo.getFileSize(), Duration.ofHours(1));
            return new UploadPreSignVO(preSignUrl);
        }
    }

    @Override
    public void setUploadProgress(Long chunkNumber) {
        Long userId = UserContext.getUser();
        UploadInfo uploadInfo = UploadInfoContext.getUploadInfo();
        String key = RedisKeyFormatter.fileUploadInfoKey(userId, uploadInfo.getUploadId());


        String bitmap = uploadInfo.getBitmap();
        char[] chars = bitmap.toCharArray();
        chars[(int) (chunkNumber - 1)] = '1';
        String s = new String(chars);
        uploadInfo.setBitmap(s);
        redisUtils.setObject(key, uploadInfo, 1L, TimeUnit.DAYS);

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadComplete(UploadCompleteDTO dto) {
        Long userId = UserContext.getUser();

        UploadInfo uploadInfo = UploadInfoContext.getUploadInfo();
        //如果是秒传
        if (dto.getIsInstant()) {
            UserFile userFile = uploadCompleteCreateUserFile(dto.getObjectId(), uploadInfo);
            //TODO 可以异步
            userClient.incrementUsedStoreSize(new UpdateUsedStoreSizeDTO(uploadInfo.getFileSize(), userId));
            return;
        }

        String eTag = null;
        //切片合并
        //获取文件eTag
        if (dto.getIsChunked() && uploadInfo.getChunkUploadId() != null) {
            eTag = uploadMerge(oss.getDefaultBucket(), uploadInfo.getObjectPath(), uploadInfo.getChunkUploadId());
        } else {
            HeadObjectResponse headObjectResponse = fileManipulator.obtainObjectMetaData(oss.getDefaultBucket(), uploadInfo.getObjectPath());
            eTag = headObjectResponse.eTag();
        }

        //存数据库
        FileObject fileObject = uploadCompleteCreateFileObject(dto.getFileMd5(), eTag, uploadInfo);
        UserFile userFile = uploadCompleteCreateUserFile(fileObject.getId(), uploadInfo);

        //文件后期处理
        AsyncUploadCompleteHandlerDTO asyncUploadCompleteHandlerDTO = new AsyncUploadCompleteHandlerDTO()
                .setFileMd5(dto.getFileMd5())
                .setFileName(userFile.getFileName())
                .setObjectPath(uploadInfo.getObjectPath())
                .setBucketName(oss.getDefaultBucket())
                .setContentType(uploadInfo.getContentType())
                .setFileSize(uploadInfo.getFileSize())
                .setUserId(userId);
        rabbitTemplate.convertAndSend(RabbitmqFileConstants.EXCHANGE_TOPIC_FILE, RabbitmqFileConstants.RK_UPLOAD_POST_PROCESS, asyncUploadCompleteHandlerDTO);

    }

    //分片上传合并
    private String uploadMerge(String bucketName, String objectPath, String uploadId) {
        try {
            //合并分片
            List<Part> parts = fileManipulator.listParts(bucketName, objectPath, uploadId);
            List<CompletedPart> completedParts = new ArrayList<>();
            for (Part part : parts) {
                CompletedPart completedPart = CompletedPart.builder().partNumber(part.partNumber()).eTag(part.eTag()).build();
                completedParts.add(completedPart);
            }
            return fileManipulator.completeMultipartUpload(bucketName, objectPath, uploadId, completedParts);
        } catch (Exception e) {
            //清理分片

            fileManipulator.abortMultipartUpload(bucketName, objectPath, uploadId);
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "上传失败");
        }
    }

    //上传完成创建文件物理表
    private FileObject uploadCompleteCreateFileObject(String fileMd5, String eTag, UploadInfo uploadInfo) {
        Long userId = UserContext.getUser();

        //查询是否有相同文件
        LambdaQueryWrapper<FileObject> queryWrapper = new LambdaQueryWrapper<FileObject>()
                .eq(FileObject::getFileMd5, fileMd5).select();
        FileObject fileObject = fileObjectMapper.selectOne(queryWrapper);

        //有相同文件增加引用， 没有则添加
        if (fileObject == null) {
            fileObject = new FileObject()
                    .setBucketName(oss.getDefaultBucket())
                    .setObjectPath(uploadInfo.getObjectPath())
                    .setFileMd5(fileMd5)
                    .setETag(eTag)
                    .setFileSize(uploadInfo.getFileSize())
                    .setContentType(uploadInfo.getContentType())
                    .setRefCount(1)
                    .setUploadUserId(userId)
                    .setIsDeleted(0L);
            fileObjectMapper.insert(fileObject);
        } else {
            fileObjectMapper.incrementRefCount(fileMd5);
        }
        return fileObject;
    }

    //上传完成创建用户文件逻辑表
    private UserFile uploadCompleteCreateUserFile(Long objectId, UploadInfo uploadInfo) {
        Long userId = UserContext.getUser();

        //获取当前目录下所有文件名
        LambdaQueryWrapper<UserFile> queryWrapper = new LambdaQueryWrapper<UserFile>()
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getParentId, uploadInfo.getParentId())
                .eq(UserFile::getDeleted, 0);
        Set<String> fileNameSet = userFileMapper.selectList(queryWrapper)
                .stream()
                .map(UserFile::getFileName)
                .collect(Collectors.toSet());

        //获取完全文件名
        String safeFileName = fileUtils.getSafeFileName(uploadInfo.getFileName(), fileNameSet);

        //构建表数据
        UserFile userFile = new UserFile()
                .setUserId(userId)
                .setParentId(uploadInfo.getParentId())
                .setObjectId(objectId)
                .setFileName(safeFileName)
                .setFileSize(uploadInfo.getFileSize())
                .setIsDirectory(false)
                .setMediaCategory(FileTypeEnum.getFileTypeEnum(uploadInfo.getContentType()))
                .setSort(0)
                .setDeleted(0L);

        userFileMapper.insert(userFile);
        return userFile;
    }
    @Override
    public void cancelChunkUpload(String bucketName, String objectPath, String uploadId){
        fileManipulator.abortMultipartUpload(bucketName, objectPath, uploadId);
    }


    @Override
    public String downloadFile(Long id) {
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
        String objectPath = fileBase.getObjectPath();

        //获取预签名url
        return fileManipulator.generateDownloadPreSignedUrl(oss.getDefaultBucket(), objectPath, fileName, contentType, Duration.ofHours(1));
    }


    @Override
    public String previewFile(Long fileId) {
        Long userId = UserContext.getUser();
        //查文件物理表
        FileObject fileObject = fileObjectMapper.selectById(fileId);
        if (fileObject == null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "数据不存在");
        }
        //查用户文件逻辑表
        LambdaQueryWrapper<UserFile> previewWrapper = new LambdaQueryWrapper<UserFile>()
                .eq(UserFile::getObjectId, fileId)
                   .eq(UserFile::getDeleted, 0);
        UserFile userFile = userFileMapper.selectOne(previewWrapper);
        if (userFile == null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "数据不存在");
        }

        return fileManipulator.generatePreviewPreSignedUrl(fileObject.getBucketName(), fileObject.getObjectPath(), fileObject.getContentType(), Duration.ofHours(1));

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

            String thumbnailObjectPath = fileUtils.getThumbnailFileStorePath(fileObject.getFileMd5(), record.getFileName());
            //获取缩略图签名
            String thumbnailUrl = fileManipulator.generatePreviewPreSignedUrl(oss.getDefaultBucket(), thumbnailObjectPath, fileObject.getContentType(), Duration.ofHours(1));
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
    public void asyncUploadFilePostHandle(AsyncUploadCompleteHandlerDTO dto) {

        //累加用户已使用空间
        userClient.incrementUsedStoreSize(new UpdateUsedStoreSizeDTO(dto.getFileSize(), dto.getUserId()));

        boolean isImage = dto.getContentType().split("/")[0].equals("image");
        String thumbnailFileStorePath = fileUtils.getThumbnailFileStorePath(dto.getFileMd5(), dto.getFileName());

        //制作图片缩略图
        if (isImage) {
            InputStream inputStream = fileManipulator.obtainDownloadInputStream(dto.getBucketName(), dto.getObjectPath(), dto.getContentType(), dto.getFileName());
            byte[] thumbnailBytes = thumbnailUtils.createThumbnailBytes(inputStream);
            try {
                fileManipulator.standardUpload(thumbnailBytes, dto.getBucketName(), thumbnailFileStorePath, dto.getContentType());
            } catch (Exception e) {
                log.error("缩略图上传失败");
                throw new CommonException(ErrorCode.OSS_ERROR.getCode(), "上传失败");
            }
        }
        //TODO提取视频封面

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
            log.error("gp.file-service.chunk-strategy-map配置读取失败");
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
     * 判断当前目录是否存在相同文件名文件
     */
    private UserFile fileNameExist(Long parentId, String fileName) {
        Long userId = UserContext.getUser();
        LambdaQueryWrapper<UserFile> wrapper = new LambdaQueryWrapper<UserFile>()
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getParentId, parentId)
                .eq(UserFile::getFileName, fileName)
                .eq(UserFile::getDeleted, 0);
        return userFileMapper.selectOne(wrapper);
    }
}
