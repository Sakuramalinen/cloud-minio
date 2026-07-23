package com.gp_01.file.model.domain.vo;

import lombok.Data;

import java.util.Map;

@Data
public class UploadFileVO {

    private Boolean isUpload;

    private String uploadId;

    private Map<Integer, String> urls;



}
