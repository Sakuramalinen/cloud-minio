package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadCompleteDTO {



    @NotNull
    @SchemaProperty(name="是否是秒传")
    private Boolean isInstant;

    @SchemaProperty(name = "文件物理id")
    private Long ObjectId;

    @NotNull
    @SchemaProperty(name = "是否是分片上传")
    private Boolean isChunked;

    @NotBlank
    @SchemaProperty(name = "文件md5")
    private String fileMd5;






}
