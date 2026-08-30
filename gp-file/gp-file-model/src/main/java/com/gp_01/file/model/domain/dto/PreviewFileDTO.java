package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PreviewFileDTO {
    @NotNull
    @SchemaProperty(name = "目录文件id")
    private Long userFileId;

    @NotNull
    @SchemaProperty(name = "文件大小")
    private Long fileSize;

    @NotBlank
    @SchemaProperty(name = "文件MIME类型")
    private String contentType;

}
