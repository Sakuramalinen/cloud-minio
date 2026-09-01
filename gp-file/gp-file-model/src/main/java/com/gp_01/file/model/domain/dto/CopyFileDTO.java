package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;

@Data
public class CopyFileDTO {

    private Long originalId;

    private Long targetId;
}
