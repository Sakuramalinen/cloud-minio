package com.gp_01.file.model.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadCompleteDTO {

    @NotNull
    private Long taskId;

}
