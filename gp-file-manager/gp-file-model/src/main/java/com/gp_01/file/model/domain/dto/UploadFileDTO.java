package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

@Data
public class UploadFileDTO{

    @SchemaProperty(name = "文件名")
    private String fileName;

    @SchemaProperty(name = "父级目录")
    private Long parentFileId;

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

    @SchemaProperty(name = "是否启用后端校验")
    private Boolean enableVerify = false;



}
