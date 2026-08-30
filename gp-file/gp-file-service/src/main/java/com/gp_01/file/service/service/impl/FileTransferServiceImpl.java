package com.gp_01.file.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gp_01.auth.encrypt_sdk.utils.EncryptUtils;
import com.gp_01.common.context.UploadInfoContext;
import com.gp_01.common.context.UserContext;
import com.gp_01.common.domain.Result;
import com.gp_01.common.domain.context.UploadInfo;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.enums.FileTypeEnum;
import com.gp_01.common.enums.RequestHeaderEnum;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.common.exception.CommonException;
import com.gp_01.file.model.domain.dto.*;
import com.gp_01.file.model.domain.po.FileObject;
import com.gp_01.file.model.domain.po.UploadTaskRecord;
import com.gp_01.file.model.domain.po.UserFile;
import com.gp_01.file.model.domain.vo.PreviewImagesVO;
import com.gp_01.file.model.domain.vo.UploadFileVO;
import com.gp_01.file.model.domain.vo.UploadPreSignVO;
import com.gp_01.file.service.constants.RabbitmqFileConstants;
import com.gp_01.file.service.mapper.FileObjectMapper;
import com.gp_01.file.service.mapper.UploadTaskRecordMapper;
import com.gp_01.file.service.mapper.UserFileMapper;
import com.gp_01.file.service.oss.OSS;
import com.gp_01.file.service.oss.download.Downloader;
import com.gp_01.file.service.oss.preview.Previewer;
import com.gp_01.file.service.oss.upload.Uploader;
import com.gp_01.file.service.service.IFileTransferService;
import com.gp_01.file.service.util.*;
import com.gp_01.user.api.client.UserClient;
import com.gp_01.user.model.domain.dto.UpdateUsedStoreSizeDTO;
import com.gp_01.user.model.domain.po.User;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileTransferServiceImpl implements IFileTransferService {

    private final UploadTaskRecordMapper uploadTaskRecordMapper;

    private final Downloader downloader;

    private final Previewer previewer;

    private final Uploader uploader;

    private final OSS oss;

    private final FileUtils fileUtils;

    private final UserFileMapper userFileMapper;

    private final FileObjectMapper fileObjectMapper;

    private final UserClient userClient;

    private final ThumbnailUtils thumbnailUtils;

    private final RabbitTemplate rabbitTemplate;

    private final EncryptUtils encryptUtils;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadFileVO uploadAuthorize(UploadAuthorizationDTO dto) {
        Long userId = UserContext.getUser();

        UploadTaskRecord task = uploadTaskRecordMapper.selectById(dto.getUploadTaskId());
        if (task == null) {
            throw new BadRequestException(ErrorCode.PARAM_ERROR.getCode(), "上传任务不存在");
        }



        //秒传判断
        FileObject fileObject = fileObjectMapper.selectOne(new LambdaQueryWrapper<FileObject>().eq(FileObject::getFileMd5, task.getFileMd5()));
        if (fileObject != null) {
            return new UploadFileVO(true);
        }
        //获取分片上传id
        if (task.getIsChunked()) {
            String uploadId = uploader.getUploadId(task.getBucketName(), task.getObjectPath());
            task.setUploadId(uploadId);
            LambdaUpdateWrapper<UploadTaskRecord> updateWrapper = new LambdaUpdateWrapper<UploadTaskRecord>()
                    .eq(UploadTaskRecord::getTaskId, dto.getUploadTaskId())
                    .eq(UploadTaskRecord::getUserId, userId)
                    .set(UploadTaskRecord::getUploadId, uploadId);
            uploadTaskRecordMapper.update(updateWrapper);
        }

        //创建token载荷信息
        UploadInfo uploadInfo = new UploadInfo(task.getUploadId(), task.getObjectPath());
        Map<String, Object> claims = new HashMap<>();
        try {
            String jsonString = new ObjectMapper().writeValueAsString(uploadInfo);
            claims.put(RequestHeaderEnum.UPLOAD_AUTHORIZATION.getCustomHeaderName(), jsonString);
        } catch (JsonProcessingException e) {
            log.error("对象转jsonString失败: object: {}", uploadInfo);
            throw new CommonException(ErrorCode.SERVICE_ERROR);
        }

        //获取私钥
        String privateKey = encryptUtils.getPrivateKeys().get("upload");
        RSAPrivateKey rsaPrivateKey = encryptUtils.readPrivateKey(privateKey);

        //创建token
        String token = encryptUtils.JwtEncrypt(claims, rsaPrivateKey, 10L, TimeUnit.MINUTES);

        return new UploadFileVO(token);

    }

    @Override
    public UploadPreSignVO getUploadPreSignedUrl(UploadPreSignDTO dto) {
        Boolean isChunk = dto.getIsChunked();
        //从上下文中获取uploadInfo
        UploadInfo uploadInfo = UploadInfoContext.getUploadInfo();

        //判断是否分片
        if (isChunk) {
            Map<Integer, String> chunkPreSignUrls = uploader.uploadChunkPreSign(oss.getBucketName(), uploadInfo.getObjectPath(), uploadInfo.getUploadId(), dto.getChunkNumbers(), 10, TimeUnit.MINUTES);
            return new UploadPreSignVO(chunkPreSignUrls);
        } else {
            String preSignUrl = uploader.uploadPreSign(oss.getBucketName(), uploadInfo.getObjectPath(), 10, TimeUnit.MINUTES);
            return new UploadPreSignVO(preSignUrl);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadComplete(UploadCompleteDTO dto) {
        Long taskId = dto.getTaskId();
        Long userId = UserContext.getUser();
        LambdaQueryWrapper<UploadTaskRecord> queryWrapper = new LambdaQueryWrapper<UploadTaskRecord>()
                .eq(UploadTaskRecord::getTaskId, taskId)
                .eq(UploadTaskRecord::getUserId, userId);
        //获取上传任务详细信息
        UploadTaskRecord task = uploadTaskRecordMapper.selectOne(queryWrapper);
        if (task == null) {
            throw new BadRequestException(ErrorCode.PARAM_ERROR.getCode(), "上传任务不存在");
        }
        //切片合并
        if (task.getIsChunked() && task.getUploadId() != null) {
            uploadMerge(task.getBucketName(), task.getObjectPath(), task.getUploadId(), taskId);
        }
        //获取文件eTag
        FileStatus fileStatus = uploader.getFileStatus(task.getBucketName(), task.getObjectPath());

        //获取真实mime类型
        String contentType = fileUtils.getContentTypeByFileBinary(task.getObjectPath(), task.getFileName());
        fileStatus.setContentType(contentType);

        //存数据库
        UserFile userFile = uploadPersistence(task, fileStatus);

        //删除上传任务
        uploadTaskRecordMapper.deleteById(taskId);
        //累加用户已使用空间
        userClient.incrementUsedStoreSize(new UpdateUsedStoreSizeDTO(task.getFileSize()));
        //文件后期处理
        UploadFilePostHandleDTO uploadFilePostHandleDTO = new UploadFilePostHandleDTO(task.getBucketName(), contentType, userFile.getFileName(), task.getObjectPath(), task.getFileMd5());
        rabbitTemplate.convertAndSend(RabbitmqFileConstants.EXCHANGE_TOPIC_FILE, RabbitmqFileConstants.RK_UPLOAD_POST_PROCESS, uploadFilePostHandleDTO);

    }

    //分片上传合并
    private void uploadMerge(String bucketName, String objectPath, String uploadId, Long taskId) {
        //合并分片
        try {
            uploader.mergeChunk(bucketName, objectPath, uploadId);
        } catch (Exception e) {
            //标记上传任务为失败
            LambdaUpdateWrapper<UploadTaskRecord> updateWrapper = new LambdaUpdateWrapper<UploadTaskRecord>()
                    .eq(UploadTaskRecord::getTaskId, taskId)
                    .set(UploadTaskRecord::getStatus, 3);
            uploadTaskRecordMapper.update(updateWrapper);

            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "上传失败");
        } finally {
            //清理分片
            uploader.abortInCompleteMultipartUpload(bucketName, objectPath, uploadId);
        }
    }

    //上传持久化
    private UserFile uploadPersistence(UploadTaskRecord task, FileStatus fileStatus) {
        Long userId = UserContext.getUser();
        //查询是否有相同文件
        LambdaQueryWrapper<FileObject> queryWrapper = new LambdaQueryWrapper<FileObject>()
                .eq(FileObject::getFileMd5, task.getFileMd5()).select();
        FileObject fileObject = fileObjectMapper.selectOne(queryWrapper);
        //有相同文件增加引用， 没有则添加
        if (fileObject == null) {
            fileObject = new FileObject()
                    .setBucketName(task.getBucketName())
                    .setObjectPath(task.getObjectPath())
                    .setFileMd5(task.getFileMd5())
                    .setETag(fileStatus.getETag())
                    .setFileSize(task.getFileSize())
                    .setContentType(fileStatus.getContentType())
                    .setRefCount(1)
                    .setUploadUserId(userId)
                    .setIsDeleted(0L);
            fileObjectMapper.insert(fileObject);
        } else {
            fileObjectMapper.incrementRefCount(task.getFileMd5());
        }

        //添加用户文件虚拟表
        return createUserFileTable(task.getFileName(), task.getParentId(), fileStatus, fileObject.getId());


    }
    //创建用户文件逻辑关系表
    private UserFile createUserFileTable(String fileName, Long parentId, FileStatus fileStatus, Long objectId) {
        Long userId = UserContext.getUser();

        //构建表数据
        UserFile userFile = new UserFile()
                .setUserId(userId)
                .setParentId(parentId)
                .setObjectId(objectId)
                .setFileName(fileName)
                .setFileSize(fileStatus.getSize())
                .setIsDirectory(0)
                .setMediaCategory(FileTypeEnum.getFileTypeEnum(fileStatus.getContentType()))
                .setSort(0)
                .setDeleted(0L);

        //获取去除扩展名的文件名
        int lastIndexOf = fileName.lastIndexOf(".");
        String baseFileName = fileName;
        String extendName = "";
        if (lastIndexOf > 0) {
            baseFileName = fileName.substring(0, lastIndexOf);
            extendName = fileName.substring(lastIndexOf);
        }

        //统计同名文件数量
        LambdaQueryWrapper<UserFile> likeWrapper = new LambdaQueryWrapper<UserFile>()
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getParentId, parentId)
                .eq(UserFile::getDeleted, 0)
                .likeRight(UserFile::getFileName, baseFileName);
        Long count = userFileMapper.selectCount(likeWrapper);

        //文件撞名重试
        for (int i = 0; i <= 15; i++) {
            try {
                //构建安全文件名
                String safeFileName;
                if (count == 0) {
                    safeFileName = fileName;
                } else {
                    safeFileName = baseFileName + "(" + count + ")" + extendName;
                }
                userFile.setFileName(safeFileName);
                userFileMapper.insert(userFile);
                return userFile;
            } catch (DuplicateKeyException e) {
                count++;
            }
        }
        throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "文件名异常，重名上限");
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
        return downloader.downloadPreSign(oss.getBucketName(), fileName, objectPath, contentType, 10, TimeUnit.MINUTES);
    }



    @Override
    public String previewFile(Long userFileId) {
        Long userId = UserContext.getUser();
        LambdaQueryWrapper<UserFile> previewWrapper = new LambdaQueryWrapper<UserFile>().eq(UserFile::getId, userFileId).eq(UserFile::getUserId, userId)
                .eq(UserFile::getDeleted, 0);
        UserFile userFile = userFileMapper.selectOne(previewWrapper);
        if (userFile == null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "数据不存在");
        }
        FileObject fileObject = fileObjectMapper.selectById(userFile.getObjectId());
        if (fileObject == null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "数据不存在");
        }

        return previewer.previewPreSignUrl(fileObject.getBucketName(), fileObject.getObjectPath(), fileObject.getContentType(), 10, TimeUnit.MINUTES);

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
            String thumbnailUrl = previewer.previewPreSignUrl(oss.getBucketName(), fileObject.getObjectPath(), fileObject.getContentType(), 10, TimeUnit.MINUTES);
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
    public void uploadFilePostHandle(UploadFilePostHandleDTO dto) {

        boolean isImage = dto.getContentType().split("/")[0].equals("image");

        String fileExtendName = fileUtils.getFileExtendName(dto.getFileName());
        String thumbnailFileStorePath = fileUtils.getThumbnailFileStorePath(dto.getFileMd5(), fileExtendName);
        //TODO制作图片缩略图
        if (isImage) {
            InputStream inputStream = downloader.getDownloadInputStream(dto.getBucketName(), dto.getObjectPath());
            byte[] thumbnailBytes = thumbnailUtils.createThumbnailBytes(inputStream);
            try {
                uploader.uploadByBytes(thumbnailBytes, dto.getBucketName(), thumbnailFileStorePath, dto.getContentType());
            } catch (MinioException e) {
                log.error("缩略图上传失败");
                throw new CommonException(ErrorCode.OSS_ERROR.getCode(), "上传失败");
            }
        }
        //TODO提取视频封面

    }



}
