package com.gp_01.common.domain.dto;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileDownloadDTO {

    @SchemaProperty(name = "文件存储路径")
    private String storePath;

    @SchemaProperty(name = "登录用户")
    private Long userId;
}
