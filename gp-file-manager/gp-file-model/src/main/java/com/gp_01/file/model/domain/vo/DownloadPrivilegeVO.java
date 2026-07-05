package com.gp_01.file.model.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(name = "获取用户凭证参数")
public class DownloadPrivilegeVO {
    @SchemaProperty(name = "文件存储路径")
    private String originalPath;

    @SchemaProperty(name = "登录用户id")
    private Long userId;
}
