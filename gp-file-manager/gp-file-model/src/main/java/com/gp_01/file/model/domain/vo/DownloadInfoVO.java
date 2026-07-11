package com.gp_01.file.model.domain.vo;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DownloadInfoVO {
    
    @SchemaProperty(name = "文件下载地址")
    private String DownloadPath;

    @SchemaProperty(name = "下载用户id")
    private Long userId;
}
