package com.gp_01.file.model.domain.vo;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadVO {
    @SchemaProperty(name = "是否已经完整上传成功")
    private Boolean uploaded;

    @SchemaProperty(name = "上传进度")
    private Long progress;

    @SchemaProperty(name = "文件id")
    private Long fileId;


}
