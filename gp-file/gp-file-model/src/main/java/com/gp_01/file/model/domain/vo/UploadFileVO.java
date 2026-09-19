package com.gp_01.file.model.domain.vo;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UploadFileVO {


    @SchemaProperty(name = "上传id")
    private String uploadId;

    @SchemaProperty(name = "是否分片")
    private Boolean isChunked;

    @SchemaProperty(name = "切片大小")
    private Long chunkSize;

    @SchemaProperty(name = "切片数量")
    private Long totalChunk;

//    @SchemaProperty(name = "上传授权token")
//    private String token;

}
