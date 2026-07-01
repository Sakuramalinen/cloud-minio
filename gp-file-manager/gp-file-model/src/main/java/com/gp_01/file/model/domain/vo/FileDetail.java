package com.gp_01.file.model.domain.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.gp_01.common.enums.FileTypeEnum;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data

public class FileDetail {
    @SchemaProperty(name = "主键")
    private Long id;

    @SchemaProperty(name = "文件id")
    private Long fileId;

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

    @SchemaProperty(name = "文件MD5值")
    private String fileMd5;


    @SchemaProperty(name = "创建时间")
    private LocalDateTime createTime;

    @SchemaProperty(name = "修改时间")
    private LocalDateTime updateTime;

}
