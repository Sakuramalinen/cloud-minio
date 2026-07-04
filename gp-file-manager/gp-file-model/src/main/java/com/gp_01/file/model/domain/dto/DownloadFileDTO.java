package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DownloadFileDTO {
    @SchemaProperty(name = "id")
    private Long Id;

    @NotNull
    @SchemaProperty(name = "是否分片")
    private Boolean chunked;

    @NotBlank
    @SchemaProperty(name = "文件名")
    private String fileName;

    @NotNull
    @SchemaProperty(name = "文件大小")
    private Long fileSize;

    @NotBlank
    @SchemaProperty(name = "文件MIME类型")
    private String contentType;

}
