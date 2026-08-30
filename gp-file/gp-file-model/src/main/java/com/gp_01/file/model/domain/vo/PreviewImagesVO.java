package com.gp_01.file.model.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "图片预览信息")
public class PreviewImagesVO {

    @SchemaProperty(name = "文件id")
    private Long fileId;

    @SchemaProperty(name = "文件或文件夹名称")
    private String fileName;

    @SchemaProperty(name = "文件大小字节")
    private Long fileSize;

    @SchemaProperty(name = "缩略图临时签名url")
    private String thumbUrl;

    @SchemaProperty(name = "创建时间")
    private LocalDateTime createTime;



}
