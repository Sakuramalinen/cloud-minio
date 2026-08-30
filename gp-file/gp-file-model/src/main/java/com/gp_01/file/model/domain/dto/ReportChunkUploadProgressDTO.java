package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "保存分片进度参数")
public class ReportChunkUploadProgressDTO {

    @SchemaProperty(name = "分片上传id")
    @NotBlank
    private String uploadId;

    @SchemaProperty(name = "当前分片序号")
    @NotNull
    private Integer currentChunkIndex;

    @SchemaProperty(name = "上传成功的ETag")
    @NotBlank
    private String ETag;

}
