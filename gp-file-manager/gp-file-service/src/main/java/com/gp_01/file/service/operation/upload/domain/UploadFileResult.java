package com.gp_01.file.service.operation.upload.domain;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileResult {

    @SchemaProperty(name = "是否已经完整上传成功")
    private Boolean uploaded;

    @SchemaProperty(name = "上传进度")
    private Long progress;
//
//    @SchemaProperty(name = "存储路径")
//    private String storePath;
}
