package com.gp_01.file.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

@Data
@Schema(name = "图片预览信息")
public class PreviewImagesVO {

    @SchemaProperty(name = "主键")
    private Long id;

    @SchemaProperty(name = "文件id")
    private Long fileId;

    @SchemaProperty(name = "文件或文件夹名称")
    private String fileName;

    @SchemaProperty(name = "文件大小字节")
    private Long fileSize;

    @SchemaProperty(name = "缩略图临时签名url")
    private String thumbUrl;

    @SchemaProperty(name = "源文件临时签名url")
    private String originalUrl;

    @SchemaProperty(name = "文件年份")
    private Integer year;

    @SchemaProperty(name = "文件月份")
    private Integer month;

    @SchemaProperty(name = "文件日份")
    private Integer day;
}
