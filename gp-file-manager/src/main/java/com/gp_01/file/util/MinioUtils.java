package com.gp_01.file.util;

import com.gp_01.file.config.MinioConfig;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

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

    /**
     * 文件上传
     * TODO 大文件断点续传
     *
     * @param file
     * @param fileName
     */
    public void uploadFile(MultipartFile file, String fileName) {
        try {
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .contentType(file.getContentType())
                    .stream(inputStream, file.getSize(), -1)
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
     * @param fileName
     */
    public void removeFile(String fileName){
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

}
