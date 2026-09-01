package com.gp_01.file.service.oss.upload.product;


import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.common.exception.CommonException;
import com.gp_01.file.service.oss.upload.Uploader;

import com.gp_01.file.service.util.FileStatus;
import io.minio.*;
import io.minio.errors.*;

import io.minio.messages.ListPartsResult;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


//@RequiredArgsConstructor
//@Component("minio_uploader")
@Component
@Slf4j
public class MinioUploader implements Uploader {
    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioAsyncClient minioAsyncClient;



    @Override
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

    @Override
    public String uploadByBytes(byte[] fileBytes, String bucketName, String objectPath, String contentType) throws MinioException {
        PutObjectArgs args = PutObjectArgs.builder().bucket(bucketName)
                .object(objectPath)
                .contentType(contentType)
                .data(fileBytes, fileBytes.length)
                .build();
        return minioClient.putObject(args).etag();
    }

    @Override
    public String uploadPreSign(String bucketName, String objectPath, Integer expiry, TimeUnit timeUnit) {
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
    @Override
    public Map<Integer, String> uploadChunkPreSign(String bucketName, String objectPath, String uploadId, List<Integer> chunkNumbers, Integer expiry, TimeUnit timeUnit) {
        Map<Integer, String> urls = new HashMap<>();
        for (Integer chunkNumber : chunkNumbers) {
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
    @Override
    public String mergeChunk(String bucketName, String objectPath, String uploadId) {
        //获取所有分片
        List<Part> parts = getParts(bucketName, objectPath, uploadId);

        if (parts.isEmpty()) {
            log.error("获取上传id:{}, 文件路径为:{}切片失败", uploadId, objectPath);
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "获取该文件切片失败");
        }
        try {
            Part[] p = new Part[parts.size()];
            for (int i = 0; i < parts.size(); i++) {
                p[i] = parts.get(i);
            }
            CompleteMultipartUploadArgs args = CompleteMultipartUploadArgs.builder()
                    .bucket(bucketName)
                    .uploadId(uploadId)
                    .object(objectPath)
                    .parts(p)
                    .build();
            ObjectWriteResponse objectWriteResponse = minioAsyncClient.completeMultipartUpload(args).get(60L, TimeUnit.SECONDS);
            return objectWriteResponse.etag();
        } catch (Exception e) {
            log.error("合并分片失败, path: {}", objectPath, e);
            throw new CommonException(ErrorCode.BUSINESS_ERROR.getCode(), "文件校验失败");
        }
    }

    /**
     * 获取所有分片
     */
    private List<Part> getParts(String bucketName, String objectPath, String uploadId){
        List<Part> parts = new ArrayList<>();
        try {
            Integer partNumberMarker = null;
            do {
                //获取所有分片
                ListPartsArgs partsArgs = ListPartsArgs.builder()
                        .bucket(bucketName)
                        .maxParts(100)
                        .uploadId(uploadId)
                        .partNumberMarker(partNumberMarker)
                        .build();
                //TODO 通过反射设置字段
                Field objectName = ObjectArgs.class.getDeclaredField("objectName");
                objectName.setAccessible(true);
                objectName.set(partsArgs, objectPath);

                ListPartsResult result = minioAsyncClient.listParts(partsArgs).get().result();

                parts.addAll(result.parts());
                partNumberMarker = result.nextPartNumberMarker();
            } while (partNumberMarker != null && partNumberMarker != 0);
        } catch (Exception e) {
            log.error("获取分片失败, path: {}", objectPath, e);
            throw new CommonException(ErrorCode.BUSINESS_ERROR.getCode(), "获取分片失败");
        }
        return parts;
    }

    @Override
    public void abortInCompleteMultipartUpload(String bucketName, String objectPath, String uploadId) {
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


    @Override
    public FileStatus getFileStatus(String bucketName, String objectPath) {
        try {
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectPath)
                    .build();
            StatObjectResponse response = minioClient.statObject(args);
            return new FileStatus()
                    .setSize(response.size())
                    .setObject(response.object())
                    .setContentType(response.contentType())
                    .setBucket(response.bucket())
                    .setETag(response.etag());
        } catch (MinioException e) {
            log.error("获取文件存储状态失败 -> bucket:{}, object:{}", bucketName, objectPath, e);
            throw new CommonException(ErrorCode.MIDDLEWARE_ERROR);
        }
    }



}
