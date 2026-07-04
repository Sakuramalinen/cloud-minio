package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoveFileDTO {
    @NotNull
    @Schema(name = "移动id")
    private Long fileId;

    @NotNull
    @Schema(name = "目标id")
    private Long targetId;
}
