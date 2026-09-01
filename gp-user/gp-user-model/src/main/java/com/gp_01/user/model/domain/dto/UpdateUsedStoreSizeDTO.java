package com.gp_01.user.model.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateUsedStoreSizeDTO {
    private Long fileSize;
    private Long userId;
}
