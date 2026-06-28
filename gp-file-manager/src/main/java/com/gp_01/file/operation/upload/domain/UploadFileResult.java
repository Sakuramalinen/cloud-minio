package com.gp_01.file.operation.upload.domain;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

@Data

public class UploadFileResult {

    @SchemaProperty(name = "是否已经完整上传成功")
    private Boolean uploaded;

    @SchemaProperty(name = "上传进度")
    private Long progress;
}
