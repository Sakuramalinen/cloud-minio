package com.gp_01.file.model.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "查询历史头像列表")
public class ListHistoryAvatarVO {
    @SchemaProperty(name = "头像id")
    private Long id;
    @SchemaProperty(name = "头像预签名访问地址")
    private String url;
    @SchemaProperty(name = "文件大小")
    private Long size;
}
