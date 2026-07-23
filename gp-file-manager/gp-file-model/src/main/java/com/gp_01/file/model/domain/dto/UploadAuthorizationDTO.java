package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadAuthorizationDTO {

    @NotNull
    @SchemaProperty(name = "父级目录Id")
    private Long parentId;

    @NotBlank
    @SchemaProperty(name = "文件名")
    private String fileName;

    @NotBlank
    @SchemaProperty(name = "文件md5值")
    private String fileMd5;

    @NotNull
    @SchemaProperty(name = "文件大小")
    private Long fileSize;

    @NotNull
    @SchemaProperty(name = "是否分片")
    private Boolean isSlice;

}
