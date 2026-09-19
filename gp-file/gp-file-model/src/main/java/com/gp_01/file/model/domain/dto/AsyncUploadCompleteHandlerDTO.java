package com.gp_01.file.model.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AsyncUploadCompleteHandlerDTO {
    private String bucketName;
    private String contentType;
    private String fileName;
    private String objectPath;
    private String fileMd5;
    private Long fileSize;
    private Long userId;



}
