package com.gp_01.file.model.domain.dto.taskRecord;

import lombok.Data;

@Data
public class CreateUploadTaskRecordDTO {

    private Long parentId;
    private Long fileSize;
    private String fileName;
    private String fileMd5;
}
