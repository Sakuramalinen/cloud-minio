package com.gp_01.file.model.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadFilePostHandleDTO {
    private String bucketName;
    private String downloadObjectPath;
    private String uploadObjectPath;
    private String contentType;


}
