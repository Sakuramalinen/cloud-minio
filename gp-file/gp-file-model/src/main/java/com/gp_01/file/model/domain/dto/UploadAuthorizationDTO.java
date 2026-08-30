package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadAuthorizationDTO {


    @NotNull
    @SchemaProperty(name = "上传任务id")
    private long uploadTaskId;


}
