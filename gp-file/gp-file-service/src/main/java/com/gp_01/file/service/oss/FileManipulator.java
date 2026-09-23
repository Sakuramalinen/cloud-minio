package com.gp_01.file.service.oss;


import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.Part;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

public interface FileManipulator {

    /**
     * 小文件上传
     *
     * @param fileBytes   文件内容
     * @param contentType 文件mime类型
     * @param objectKey   文件key
     * @param bucketName  存储桶名
     */
    void standardUpload(byte[] fileBytes, String bucketName, String objectKey, String contentType);

    /**
     * 上传初始化
     *
     * @param bucketName 存储桶名
     * @param objectKey  文件key
     * @return 上传id
     */
    String initiateMultipartUpload(String bucketName, String objectKey, String contentType);

    /**
     * 上传合并
     *
     * @param bucketName 存储桶名
     * @param objectKey  文件key
     * @param uploadId   上传id
     * @param partETags  分片eTag集合
     * @return eTag
     */
    String completeMultipartUpload(String bucketName, String objectKey, String uploadId, List<CompletedPart> partETags);

    /**
     * 获取下载输入流
     * @param bucketName 存储桶名
     * @param objectKey  文件key
     * @param contentType 文件mime类型
     * @param fileName 文件名
     * @return 输入流
     */
    InputStream obtainDownloadInputStream(String bucketName, String objectKey, String contentType, String fileName);

    /**
     * 生成上传预签名
     *
     * @param bucketName  存储桶名
     * @param objectKey   文件key
     * @param contentType 文件mime类型
     * @param expiration  签名过期时间
     * @return url
     */
    String generateUploadPreSignedUrl(String bucketName, String objectKey, String contentType, Long contentLength, Duration expiration);

    String generateUploadPartPreSignedUrl(String bucketName, String objectKey, String uploadId, Integer partNumber, Duration expiration);

    /**
     * 生成下载预签名
     *
     * @param bucketName 存储桶名
     * @param objectKey  文件key
     * @param expiration 签名过期时间
     * @return url
     */
    String generateDownloadPreSignedUrl(String bucketName, String objectKey, String fileName, String contentType, Duration expiration);


    /**
     * 生成预览预签名
     *
     * @param bucketName  存储桶名
     * @param objectKey   文件key
     * @param expiration  签名过期时间
     * @param contentType 文件mime类型
     * @return url
     */
    String generatePreviewPreSignedUrl(String bucketName, String objectKey, String contentType, Duration expiration);

    /**
     * 取消分片上传，删除分片
     *
     * @param bucketName 存储桶名
     * @param objectKey  文件key
     * @param uploadId   上传id
     */
    void abortMultipartUpload(String bucketName, String objectKey, String uploadId);


    /**
     * 获取文件分片列表
     *
     * @param bucketName 存储桶名
     * @param objectKey  文件key
     * @param uploadId   上传id
     * @return parts
     */
    List<Part> listParts(String bucketName, String objectKey, String uploadId);

    /**
     * 删除单个对象
     *
     * @param bucketName 存储桶名
     * @param objectKey  文件key
     */
    void deleteObject(String bucketName, String objectKey);


    /**
     * 批量删除对象
     *
     * @param bucketName 存储桶名
     * @param objectKeys 文件key集合
     */
    void deleteObjectBatch(String bucketName, List<String> objectKeys);

    /**
     * 获取文件元信息
     * @param bucketName 存储桶名
     * @param objectKey 文件key集合
     * @return 元信息
     */
    HeadObjectResponse obtainObjectMetaData(String bucketName, String objectKey);

}
