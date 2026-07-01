package com.gp_01.file.service.operation.upload.domain;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

@Data
public class UploadFile {

    @SchemaProperty(name = "文件名")
    private String fileName;

    @SchemaProperty(name = "文件路径")
    private String filePath;

    @SchemaProperty(name = "当前分片编号")
    private Long currentChunkIndex;

    @SchemaProperty(name = "当前分片大小")
    private Long currentChunkSize;

    @SchemaProperty(name = "分片数量")
    private Long chunkNumber;

    @SchemaProperty(name = "标准分片大小")
    private Long chunkSize;

    @SchemaProperty(name = "分片唯一表示(md5)")
    private String chunkMd5;

    @SchemaProperty(name = "文件总大小")
    private Long fileSize;

    @SchemaProperty(name = "文件唯一标识(md5)")
    private String fileMd5;



}
