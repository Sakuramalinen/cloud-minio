package com.gp_01.common.domain.dto;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileDownloadDTO {

    @SchemaProperty(name = "下载地址")
    private String downloadPath;
    @SchemaProperty(name = "登录用户")
    private Long userId;
}
