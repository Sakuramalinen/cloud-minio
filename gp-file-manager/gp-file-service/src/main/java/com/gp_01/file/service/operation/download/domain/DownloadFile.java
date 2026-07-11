package com.gp_01.file.service.operation.download.domain;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class DownloadFile {


    @SchemaProperty(name = "桶名")
    private String bucketName;

    @SchemaProperty(name = "文件存储路径")
    private String downloadPath;

    @SchemaProperty(name = "下载起始位置")
    private Long offset;

    @SchemaProperty(name = "下载长度")
    private Long length;

    @SchemaProperty(name = "是否分片下载")
    private Boolean chunked;



    public DownloadFile(String bucketName, String downloadPath){
        this.chunked = false;

        this.bucketName = bucketName;
        this.downloadPath = downloadPath;
    }

    public DownloadFile(String bucketName, String downloadPath, Long offset, Long length){
        this.chunked = true;

        this.offset = offset;
        this.length = length;
        this.bucketName = bucketName;
        this.downloadPath = downloadPath;
    }

}
