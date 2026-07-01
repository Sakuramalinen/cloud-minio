package com.gp_01.file.service.operation.download.domian;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadFile {

    @SchemaProperty(name = "完整存储路径")
    private String integratePath;

    @SchemaProperty(name = "下载起始位置")
    private Long offset;

    @SchemaProperty(name = "下载长度")
    private Long length;

    @SchemaProperty(name = "是否分片下载")
    private Boolean chunked;

    public DownloadFile(String integratePath){
        this.chunked = false;
        this.integratePath = integratePath;
    }

}
