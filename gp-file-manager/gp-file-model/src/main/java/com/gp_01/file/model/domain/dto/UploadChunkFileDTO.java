package com.gp_01.file.model.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
@Data
public class UploadChunkFileDTO {
    @NotBlank
    private String uploadId;

    @NotNull
    private List<Integer> chunkNumbers;
}
