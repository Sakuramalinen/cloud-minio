package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadFileDTO{
    @NotBlank
    @SchemaProperty(name = "文件名")
    private String fileName;

    @NotNull
    @SchemaProperty(name = "父级目录")
    private Long parentFileId;

    @NotBlank
    @SchemaProperty(name = "文件唯一标识(md5)")
    private String fileMd5;

    @NotNull
    @SchemaProperty(name = "文件总大小")
    private Long fileSize;

    @NotNull
    @SchemaProperty(name = "分片数量")
    private Long chunkNumber;

    @NotNull
    @Min(value = 1)
    @SchemaProperty(name = "当前分片编号")
    private Long currentChunkIndex;

    @SchemaProperty(name = "当前分片大小")
    private Long currentChunkSize;








}
