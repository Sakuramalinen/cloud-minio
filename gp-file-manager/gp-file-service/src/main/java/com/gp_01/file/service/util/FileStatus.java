package com.gp_01.file.service.util;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
//TODO 文件状态 架构上不知道放在哪里
@Accessors(chain = true)
public class FileStatus {

    @SchemaProperty(name = "文件eTag")
    private String eTag;

    @SchemaProperty(name = "文件大小")
    private Long size;

    @SchemaProperty(name = "文件所属桶")
    private String bucket;

    @SchemaProperty(name = "文件key")
    private String object;

    @SchemaProperty(name = "mime类型")
    private String contentType;


}
