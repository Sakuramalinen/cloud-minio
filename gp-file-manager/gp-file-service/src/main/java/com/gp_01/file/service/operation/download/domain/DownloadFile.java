package com.gp_01.file.service.operation.download.domain;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

@Data
@AllArgsConstructor
public class DownloadFile {


    @SchemaProperty(name = "桶名")
    private String bucketName;

    @SchemaProperty(name = "文件存储路径")
    private String downloadPath;

    @SchemaProperty(name = "下载起始位置")
    private Long offset;

    @SchemaProperty(name = "下载长度")
    private Long length;

    @SchemaProperty(name = "文件MIME类型")
    private String contentType;

    @SchemaProperty(name = "文件名")
    private String fileName;

    @SchemaProperty(name = "是否分片下载")
    private Boolean chunked;

    @SchemaProperty(name = "预签名过期时间")
    private Integer expiry;

    @SchemaProperty(name = "过期时间单位")
    private TimeUnit timeUnit;

    /**
     * 直接下载构造
     * @param bucketName
     * @param downloadPath
     */
    public DownloadFile(String bucketName, String downloadPath){
        this.chunked = false;
        this.bucketName = bucketName;
        this.downloadPath = downloadPath;
    }

    /**
     * 直连OSS构造
     * @param bucketName
     * @param downloadPath
     * @param expiry 过期时间 单位分钟
     */
    public DownloadFile(String bucketName, String contentType, String fileName, String downloadPath, Integer expiry, TimeUnit timeUnit){
        this.chunked = true;
        this.contentType = contentType;
        this.fileName = fileName;
        this.bucketName = bucketName;
        this.downloadPath = downloadPath;
        this.expiry = expiry;
        this.timeUnit = timeUnit;
    }

    /**
     * 分片下载构造
     * @param bucketName
     * @param downloadPath
     * @param offset
     * @param length
     * @param chunked
     */
    public DownloadFile(String bucketName, String downloadPath, Long offset, Long length, Boolean chunked){
        this.chunked = chunked;
        this.offset = offset;
        this.length = length;
        this.bucketName = bucketName;
        this.downloadPath = downloadPath;
    }



}
