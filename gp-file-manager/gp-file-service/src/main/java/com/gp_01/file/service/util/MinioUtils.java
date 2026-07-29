package com.gp_01.file.service.util;

import com.gp_01.common.exception.CommonException;
import com.gp_01.file.service.config.MinioConfig;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.ListAllMyBucketsResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static io.minio.Http.Method.GET;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtils {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    private final MinioAsyncClient minioAsyncClient;

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
    public List<ListAllMyBucketsResult.Bucket> listBuckets() {
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
            log.error("下载文件失败：下载路径：{} -> ", storePath, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取文件ETAG
     * @param bucketName
     * @param objectPath
     * @return
     */
    public String getFielETag(String bucketName, String objectPath) {
        StatObjectArgs args = StatObjectArgs.builder()
                .bucket(bucketName)
                .object(objectPath)
                .build();
        try {
            StatObjectResponse response = minioClient.statObject(args);
            return response.etag();
        } catch (MinioException e) {
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
                    .method(GET)
                    .bucket(minioConfig.getBucketName())
                    .object(path)
                    .expiry(expireMinute, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            log.error("获取临时签名错误: 存储路径：{} -> ", path, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 清理上传中断分片文件
     * @param bucketName
     * @param objectPath
     * @param uploadId
     */
    public void abortInCompleteMultipartUpload(String bucketName, String objectPath, String uploadId){
        AbortMultipartUploadArgs args = AbortMultipartUploadArgs.builder()
                .bucket(bucketName)
                .object(objectPath)
                .uploadId(uploadId).build();
        minioAsyncClient.abortMultipartUpload(args).whenComplete((abortMultipartUploadResponse, throwable) -> {
            if (throwable != null) {
                log.error("清理中断分片文件bucket={} object={} uploadId={}", bucketName, objectPath, uploadId);
            }
        });
    }




}
