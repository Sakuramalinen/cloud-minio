package com.gp_01.file.model.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FileReNameDTO {
    @NotNull
    private Long id;
    @NotBlank
    private String fileName;
}
