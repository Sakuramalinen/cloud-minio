package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

@Data
public class DownloadFileDTO {
    @SchemaProperty(name = "id")
    private Long Id;

    @SchemaProperty(name = "是否分片")
    private Boolean chunked;

//    @SchemaProperty(name = "切片数量")
//    private Long chunkNumber;
//
//    @SchemaProperty(name = "当前切片编号")
//    private Long currentChunkIndex;

    @SchemaProperty(name = "文件名")
    private String fileName;

    @SchemaProperty(name = "文件大小")
    private Long fileSize;

    @SchemaProperty(name = "文件MIME类型")
    private String contentType;

}
