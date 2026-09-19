package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadAuthorizationDTO {

    @NotNull
    @SchemaProperty(name = "文件大小")
    private Long fileSize;

    @NotNull
    @SchemaProperty(name = "文件名")
    private String fileName;

    @NotNull
    @SchemaProperty(name = "所在目录id")
    private Long parentId;

    @NotNull
    @SchemaProperty(name = "文件mime类型")
    private String contentType;


}
