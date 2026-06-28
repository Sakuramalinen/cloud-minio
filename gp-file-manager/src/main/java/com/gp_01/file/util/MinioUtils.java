package com.gp_01.file.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gp_01.file.config.MinioConfig;
import com.gp_01.file.operation.upload.domain.UploadFile;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.ListAllMyBucketsResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtils {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;


    private static final String CHUNK_UPLOAD_SUFFIX = ".chunkUploading";


    /**
     * 判断桶是否存在
     *
     * @param bucketName
     * @return
     */
    public boolean existBucket(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("minio桶错误：", e);
            throw new RuntimeException("");
        }
    }

    /**
     * 当不存在时创建桶
     *
     * @param bucketName
     */
    public void createBucketIfNotExist(String bucketName) {
        boolean exist = existBucket(bucketName);
        if (!exist) {
            try {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } catch (Exception e) {
                log.error("创建桶错误：", e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 获取所有桶集合
     *
     * @return
     */
    public List<Bucket> listBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            log.error("获取桶集合错误：", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除桶
     *
     * @param bucketName
     */
    public void removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }


    public void uploadOriginalFile(MultipartFile file, String fileName) {
        try {
            uploadOriginalFile(file.getInputStream(), fileName, file.getContentType(), file.getSize());
        } catch (Exception e) {
            log.error("上传文件失败：", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 文件上传
     *
     * @param inputStream
     * @param fileName
     * @param contentType
     * @param size
     */
    public void uploadOriginalFile(InputStream inputStream, String fileName, String contentType, long size) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .contentType(contentType)
                    .stream(inputStream, size, -1)
                    .object(fileName)
                    .build());
        } catch (Exception e) {
            log.error("上传文件失败：", e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 文件下载
     *
     * @param integratePath
     */
    public InputStream downloadFile(String integratePath) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(integratePath)
                    .build());
        } catch (Exception e) {
            log.error("下载文件失败：", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除文件
     *
     * @param fileName
     */
    public void removeFile(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName
                    ).build());
        } catch (Exception e) {
            log.error("删除文件失败：", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量获取临时签名url
     *
     * @param path
     * @param expireMinute
     * @return
     */
    public String getTempSignedUrl(String path, int expireMinute) {
        try {
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(path)
                    .expiry(expireMinute, TimeUnit.MINUTES)
                    .build());
            return url;
        } catch (Exception e) {
            log.error("获取临时签名错误", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 上传切片
     *
     * @param file              文件
     * @param identifier        唯一标识（md5）
     * @param currentChunkIndex 当前切片序号
     * @param basePath          基本路径
     */
    public void uploadChunk(MultipartFile file, String identifier, Long currentChunkIndex, String basePath) {
        if (basePath == null || basePath.isEmpty() || identifier == null || identifier.isEmpty() || currentChunkIndex == null || file == null) {
            throw new RuntimeException("参数异常");
        }
        try {
            String fileName = identifier + "_" + currentChunkIndex + CHUNK_UPLOAD_SUFFIX;
            String integratePath = basePath + "/" + fileName;
            PutObjectArgs args = PutObjectArgs.builder().bucket(minioConfig.getTempBucketName()).object(integratePath).stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build();
            minioClient.putObject(args);
        } catch (Exception e) {
            throw new RuntimeException("minio上传失败：", e);
        }
    }

    /**
     * 合并切片
     *
     * @param chunkNumber 切片数量
     * @param basePath    基本路径
     * @param identifier  唯一标识（md5）
     */
    public void mergeFile(Long chunkNumber, String basePath, String identifier, String targetPath) {
        if (chunkNumber == null || basePath == null || basePath.isEmpty() || identifier == null || identifier.isEmpty() || targetPath == null) {
            throw new RuntimeException("参数异常");
        }
        List<ComposeSource> sources = new ArrayList<>();
        for (int i = 1; i <= chunkNumber; i++) {
            String integratePath = basePath + "/" + identifier + "_" + i + CHUNK_UPLOAD_SUFFIX;
            ComposeSource composeSource = ComposeSource.builder().bucket(minioConfig.getTempBucketName()).object(integratePath).build();
            sources.add(composeSource);
        }
        try {
//            String targetPath = basePath + "/" +  identifier + extendName;
            ComposeObjectArgs args = ComposeObjectArgs.builder().bucket(minioConfig.getBucketName()).object(targetPath).sources(sources).build();
            minioClient.composeObject(args);
        } catch (Exception e) {
            throw new RuntimeException("文件合并失败：", e);
        }
    }

    /**
     * 删除该文件所有切片
     *
     * @param chunkNumber 切片数量
     * @param basePath    基本路径
     * @param identifier  唯一标识（md5）
     */
    public void deleteChunk(Long chunkNumber, String basePath, String identifier) {
        if (chunkNumber == null || basePath == null || basePath.isEmpty() || identifier == null || identifier.isEmpty()) {
            throw new RuntimeException("参数异常");
        }
        try {
            List<DeleteObject> deletedObjects = new ArrayList<>();
            for (int i = 1; i <= chunkNumber; i++) {
                String integratePath = basePath + "/" + identifier + "_" + i + CHUNK_UPLOAD_SUFFIX;
                DeleteObject obj = new DeleteObject(integratePath);
                deletedObjects.add(obj);
            }
            RemoveObjectsArgs args = RemoveObjectsArgs.builder().bucket(minioConfig.getTempBucketName()).objects(deletedObjects).build();
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


}
