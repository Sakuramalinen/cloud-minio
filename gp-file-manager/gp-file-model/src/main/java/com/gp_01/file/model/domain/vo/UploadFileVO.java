package com.gp_01.file.model.domain.vo;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class UploadFileVO {

    @SchemaProperty(name = "是否上传成功")
    private Boolean isUpload;

    @SchemaProperty(name = "上传任务id")
    private Long taskId;

    @SchemaProperty(name = "是否采用分片上传")
    private Boolean isChunked;

    @SchemaProperty(name = "每片大小")
    private Long chunkSize;

    @SchemaProperty(name = "分片进度位图")
    private String chunkBitmap;




}
