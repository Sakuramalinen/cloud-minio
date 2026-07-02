package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

@Data
public class PreviewFileDTO {

    @SchemaProperty(name = "是否分流传输")
    private Boolean chunkStreamed;
    @SchemaProperty(name = "文件大小")
    private Long fileSize;
    @SchemaProperty(name = "文件MIME类型")
    private String contentType;

}
