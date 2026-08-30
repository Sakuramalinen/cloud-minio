package com.gp_01.file.service.oss.upload;

import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.CommonException;
import com.gp_01.file.service.util.FileStatus;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public interface  Uploader {

    //分片上传申请uploadId
    String getUploadId(String bucketName, String objectPath);

    //上传文件
    String uploadByBytes(byte[] fileBytes, String bucketName, String objectPath, String contentType) throws MinioException;

    //完整文件上传预签名
    String uploadPreSign(String bucketName, String objectPath, Integer expiry, TimeUnit timeUnit);

    //分片预签名
    Map<Integer, String> uploadChunkPreSign(String bucketName, String objectPath, String uploadId, List<Integer> chunkNumbers, Integer expiry, TimeUnit timeUnit);

    //合并分片
    String mergeChunk(String bucketName, String objectPath, String uploadId);

    //清理分片
    void abortInCompleteMultipartUpload(String bucketName, String objectPath, String uploadId);

    //获取文件详细信息
    FileStatus getFileStatus(String bucketName, String objectPath) ;






}
