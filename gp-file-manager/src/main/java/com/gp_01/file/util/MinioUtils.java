package com.gp_01.file.util;

import com.gp_01.file.config.MinioConfig;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtils {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

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
     * TODO 大文件断点续传
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
     * @param fileName
     */
    public InputStream downloadFile(String fileName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
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
}
