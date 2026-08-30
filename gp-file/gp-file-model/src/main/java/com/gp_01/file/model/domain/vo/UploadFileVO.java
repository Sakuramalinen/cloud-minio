package com.gp_01.file.model.domain.vo;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UploadFileVO {

    @SchemaProperty(name = "是否秒传成功")
    private Boolean isInstantUpload;


    @SchemaProperty(name = "上传授权token")
    private String token;


    public UploadFileVO(Boolean isInstantUpload) {
        this.isInstantUpload = isInstantUpload;
    }

    public UploadFileVO(String token) {
        this.isInstantUpload = false;
        this.token = token;
    }
}
