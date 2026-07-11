package com.gp_01.file.service.operation.upload.product;

import com.gp_01.file.model.domain.po.FileBase;
import com.gp_01.file.service.config.MinioConfig;
import com.gp_01.file.service.operation.download.domain.DownloadFile;
import com.gp_01.file.service.operation.upload.Uploader;
import com.gp_01.file.service.operation.upload.domain.UploadFile;
import com.gp_01.file.service.operation.upload.domain.UploadFileResult;
import com.gp_01.file.service.util.MinioUtils;
import com.gp_01.file.service.util.RedisUtils;
import com.gp_01.file.service.util.ThumbnailUtils;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xmlunit.builder.Input;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

//@RequiredArgsConstructor
//@Component("minio_uploader")
@Component
@Slf4j
public class MinioUploader extends Uploader {


//    @Autowired
//    private MinioUtils minioUtils;

//    @Autowired
//    private MinioConfig minioConfig;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private ThumbnailUtils thumbnailUtils;


    public MinioUploader(RedisUtils redisUtils) {
        super(redisUtils);
    }


    @Override
    public void uploadChunk(UploadFile uploadFile, String tempBucketName, InputStream inputStream) {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(tempBucketName)
                    .object(uploadFile.getUploadPath())
                    .stream(inputStream, uploadFile.getCurrentChunkSize(), -1)
                    .build();
            minioClient.putObject(args);
        } catch (Exception e) {
            throw new RuntimeException("minio上传失败：", e);
        }
    }

    @Override
    public UploadFileResult uploadBySingleFile(UploadFile uploadFile, InputStream inputStream) {
        try {
            //获取存储路径
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(uploadFile.getBucketName())
                    .stream(inputStream, uploadFile.getFileSize(), -1)
                    .object(uploadFile.getUploadPath())
                    .build();
            minioClient.putObject(args);
            return new UploadFileResult(true, 1L);
        } catch (Exception e) {
            log.error("上传文件失败：", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void mergeAllChunks(UploadFile uploadFile, String tempBucketName) {
        //准备合并参数
        List<ComposeSource> sources = new ArrayList<>();
        for (long i = 1; i <= uploadFile.getChunkNumber(); i++) {
            ComposeSource composeSource = ComposeSource.builder()
                    .bucket(tempBucketName)
                    .object(uploadFile.getUploadPath())
                    .build();
            sources.add(composeSource);
        }
        try {
            //获取合并后存储路径
            ComposeObjectArgs args = ComposeObjectArgs.builder()
                    .bucket(uploadFile.getBucketName())
                    .object(uploadFile.getUploadPath())
                    .sources(sources)
                    .build();
            //合并
            minioClient.composeObject(args);
        } catch (Exception e) {
            throw new RuntimeException("文件合并失败：", e);
        }
    }

    @Override
    public void deleteAllTempChunks(UploadFile uploadFile, String tempBucketName) {
        try {
            List<DeleteObject> deletedObjects = new ArrayList<>();
            for (long i = 1; i <= uploadFile.getChunkNumber(); i++) {
                DeleteObject obj = new DeleteObject(uploadFile.getUploadPath());
                deletedObjects.add(obj);
            }
            RemoveObjectsArgs args = RemoveObjectsArgs.builder()
                    .bucket(tempBucketName)
                    .objects(deletedObjects)
                    .build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(args);
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                if (error != null) {
                    log.error("删除切片失败：{}", error.objectName());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("切片删除错误", e);
        }
    }

//    @Override
//    public UploadFileResult uploadThumbnailFile(UploadFile uploadFile, InputStream inputStream) {
//        if (inputStream == null) {
//            String storePath = minioUtils.getOriginalFileAbsolutionPath(uploadFile.getFileMd5(), uploadFile.getExtendName());
//            inputStream = minioUtils.downloadFile(storePath);
//        }
//        //构建存储路径
//        String thumbnailStorePath = minioUtils.getThumbnailFileAbsolutionPath(uploadFile.getFileMd5(), uploadFile.getExtendName());
//        byte[] thumbnailBytes = thumbnailUtils.createThumbnailBytes(inputStream);
//        //上传缩略图
//        minioUtils.uploadFile(new ByteArrayInputStream(thumbnailBytes), (long) thumbnailBytes.length, thumbnailStorePath);
//        return new UploadFileResult(true, 1L, thumbnailStorePath);
//    }
//
//    @Override
//    public UploadFileResult uploadThumbnailFile(UploadFile uploadFile) {
//
//        String storePath = minioUtils.getOriginalFileAbsolutionPath(uploadFile.getFileMd5(), uploadFile.getExtendName());
//        InputStream inputStream = minioUtils.downloadFile(storePath);
//        //构建存储路径
//        String thumbnailStorePath = minioUtils.getThumbnailFileAbsolutionPath(uploadFile.getFileMd5(), uploadFile.getExtendName());
//        byte[] thumbnailBytes = thumbnailUtils.createThumbnailBytes(inputStream);
//        //上传缩略图
//        minioUtils.uploadFile(new ByteArrayInputStream(thumbnailBytes), (long) thumbnailBytes.length, thumbnailStorePath);
//        return new UploadFileResult(true, 1L, thumbnailStorePath);
//    }


}
