package com.gp_01.common.domain.dto;

import lombok.Data;

@Data
public class LoginUserDTO {
    private Long userId;
    private boolean rememberMe;

}
