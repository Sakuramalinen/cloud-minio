package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MoveFileDTO {
    @Schema(name = "移动id")
    private Long fileId;
    @Schema(name = "目标id")
    private Long targetId;
}
