package com.gp_01.file.service.operation.upload.product;


import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.CommonException;
import com.gp_01.file.service.operation.upload.Uploader;
import com.gp_01.file.service.operation.upload.domain.DirectConnectionUploadFileParam;

import io.minio.*;
import io.minio.errors.*;

import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


//@RequiredArgsConstructor
//@Component("minio_uploader")
@Component
@Slf4j
public class MinioUploader extends Uploader {
    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioAsyncClient minioAsyncClient;


    /**
     * 分片上传第一步，需要获取上传id
     */
    public String getUploadId(String bucketName, String objectPath) {

        CreateMultipartUploadArgs args = CreateMultipartUploadArgs.builder()
                .bucket(bucketName)
                .object(objectPath)
                .build();
        try {
            CreateMultipartUploadResponse createMultipartUploadResponse = minioAsyncClient.createMultipartUpload(args).get();
            return createMultipartUploadResponse.result().uploadId();
        } catch (Exception e) {
            log.error("上传文件获取uploadId失败, path: {}", objectPath, e);
            throw new CommonException(ErrorCode.BUSINESS_ERROR.getCode(), "生成上传链接失败");
        }
    }

    /**
     * 获取完整文件上传url
     */
    public String getWholeUploadUrl(String bucketName, String objectPath, Integer expiry, TimeUnit timeUnit) {
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .method(Http.Method.PUT)
                .expiry(expiry, timeUnit)
                .bucket(bucketName)
                .object(objectPath)
                .build();
        try {
            return minioAsyncClient.getPresignedObjectUrl(args);
        } catch (Exception e) {
            log.error("生成预签名URL失败, path: {}", objectPath, e);
            throw new CommonException(ErrorCode.BUSINESS_ERROR.getCode(), "生成上传链接失败");
        }
    }

    /**
     * 获取所有切片上传url
     */
    public Map<Integer, String> getChunkUploadUrls(String bucketName, String objectPath, String uploadId, List<Integer> chunkNumbers,  Integer expiry, TimeUnit timeUnit) {
        Map<Integer, String> urls = new HashMap<>();
        for(Integer chunkNumber : chunkNumbers){
            //构建上传url
            Map<String, String> map = new HashMap<>();
            map.put("uploadId", uploadId);
            map.put("partNumber", String.valueOf(chunkNumber));
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Http.Method.PUT)
                    .expiry(expiry, timeUnit)
                    .bucket(bucketName)
                    .object(objectPath)
                    .extraQueryParams(map)
                    .build();
            try {
                String url = minioAsyncClient.getPresignedObjectUrl(args);
                urls.put(chunkNumber, url);
            } catch (Exception e) {
                log.error("生成预签名URL失败, path: {}", objectPath, e);
                throw new CommonException(ErrorCode.BUSINESS_ERROR.getCode(), "生成上传链接失败");
            }
        }
        return urls;
    }

    /**
     * 切片合并
     */
    public String chunkFileMerge(String bucketName, String objectPath, String uploadId, Map<Integer, String> partMap) {

        Part[] parts = new Part[partMap.size()];
        int index = 0;
        for (Map.Entry<Integer, String> entry : partMap.entrySet()) {
            parts[index++] = new Part(entry.getKey(), entry.getValue());
        }

        CompleteMultipartUploadArgs args = CompleteMultipartUploadArgs.builder()
                .bucket(bucketName)
                .uploadId(uploadId)
                .object(objectPath)
                .parts(parts)
                .build();
        try {
            ObjectWriteResponse objectWriteResponse = minioAsyncClient.completeMultipartUpload(args).get();
            return objectWriteResponse.etag();
        } catch (Exception e) {
            log.error("合并分片失败, path: {}", objectPath, e);
            throw new CommonException(ErrorCode.BUSINESS_ERROR.getCode(), "文件校验失败");
        }
    }
}
