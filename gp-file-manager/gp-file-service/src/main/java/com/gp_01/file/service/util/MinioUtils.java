package com.gp_01.file.service.util;

import com.gp_01.file.service.config.MinioConfig;
import com.gp_01.file.service.constants.MinioConstants;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
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
     */
    public void removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }


    /**
     * 文件直接下载
     *
     * @param storePath 文件存储路径
     */
    public InputStream downloadFile(String storePath) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(storePath)
                    .build());
        } catch (Exception e) {
            log.error("下载文件失败：下载路径：{} -> ",storePath, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 范围下载
     *
     * @param storePath 文件存储路径
     * @param offset 下载起始地址
     * @param len 下载长度
     * @return 分片输入流
     */
    public InputStream downloadFile(String storePath, Long offset, Long len) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(storePath)
                    .offset(offset)
                    .length(len)
                    .build());
        } catch (Exception e) {
            log.error("下载文件失败：下载路径：{} -> ",storePath, e);
            throw new RuntimeException(e);
        }
    }

    public void uploadFile(InputStream inputStream, Long fileSize, String storePath) {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .stream(inputStream, fileSize, -1)
                    .object(storePath)
                    .build();
            minioClient.putObject(args);
        } catch (Exception e) {
            log.error("上传文件失败：存储路径：{} -> ", storePath, e);
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
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(path)
                    .expiry(expireMinute, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            log.error("获取临时签名错误: 存储路径：{} -> ", path, e);
            throw new RuntimeException(e);
        }
    }



}
