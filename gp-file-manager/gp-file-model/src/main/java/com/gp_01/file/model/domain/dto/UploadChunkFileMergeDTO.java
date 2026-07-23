package com.gp_01.file.model.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class UploadChunkFileMergeDTO {

    @NotBlank
    private String uploadId;

    @NotNull
    private Map<Integer, String> parts;
}


