package com.gp_01.common.domain.context;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UploadInfo {

    @SchemaProperty(name = "上传id")
    private String uploadId;

    @SchemaProperty(name = "分片上传id")
    private String chunkUploadId;

    @SchemaProperty(name = "上传路径")
    private String objectPath;

    @SchemaProperty(name = "文件大小")
    private Long fileSize;

    @SchemaProperty(name = "文件名")
    private String fileName;

    @SchemaProperty(name = "所在目录id")
    private Long parentId;

    @SchemaProperty(name = "文件mime类型")
    private String contentType;

    @SchemaProperty(name = "上传进度")
    private String bitmap;

//    @SchemaProperty(name = "分片总数量")
//    private Long chunkTotal;


}
