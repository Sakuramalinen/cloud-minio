package com.gp_01.file.model.domain.vo;

import com.gp_01.common.enums.FileTypeEnum;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ListRecycleBinVO {
    @SchemaProperty(name = "主键")
    private Long id;

    @SchemaProperty(name = "用户id")
    private Long userId;

    @SchemaProperty(name = "文件id")
    private Long fileId;

    @SchemaProperty(name = "父文件夹ID(根目录规定为0)")
    private Long parentId;

    @SchemaProperty(name = "文件或文件夹名称")
    private String fileName;

    @SchemaProperty(name = "文件后缀名")
    private String fileSuffix;

    @SchemaProperty(name = "文件大小字节")
    private Long fileSize;

    @SchemaProperty(name = "MIME类型")
    private String contentType;

    @SchemaProperty(name = "文件类型")
    private FileTypeEnum fileType;

    @SchemaProperty(name = "有效天数")
    private Long validDay;

    @SchemaProperty(name = "删除时间")
    private LocalDateTime deleteTime;

}
