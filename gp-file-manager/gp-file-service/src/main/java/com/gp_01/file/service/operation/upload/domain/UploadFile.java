package com.gp_01.file.service.operation.upload.domain;

import com.gp_01.file.service.operation.download.Downloader;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFile {

    @SchemaProperty(name = "文件唯一标识(md5)")
    private String fileMd5;

    @SchemaProperty(name = "桶名")
    private String bucketName;

    @SchemaProperty(name = "上传路径")
    private String uploadPath;

    @SchemaProperty(name = "当前分片编号")
    private Long currentChunkIndex;

    @SchemaProperty(name = "分片数量")
    private Long chunkNumber;

    @SchemaProperty(name = "当前切片大小")
    private Long currentChunkSize;

    @SchemaProperty(name = "文件总大小")
    private Long fileSize;

    @SchemaProperty(name = "是否分片上传")
    private Boolean isChunk;

    /**
     * 文件整体上传构造方法
     */
    public UploadFile(String uploadPath, Long fileSize, String bucketName){
        this.uploadPath = uploadPath;
        this.fileSize = fileSize;
        this.bucketName = bucketName;
        this.isChunk = false;
    }

    /**
     * 文件分片上传构造方法
     */
    public UploadFile(String fileMd5, String bucketName, String uploadPath, Long currentChunkIndex, Long chunkNumber, Long currentChunkSize, Long fileSize) {
        this.fileMd5 = fileMd5;
        this.bucketName = bucketName;
        this.uploadPath = uploadPath;
        this.currentChunkIndex = currentChunkIndex;
        this.chunkNumber = chunkNumber;
        this.currentChunkSize = currentChunkSize;
        this.fileSize = fileSize;
        this.isChunk = true;
    }
}
