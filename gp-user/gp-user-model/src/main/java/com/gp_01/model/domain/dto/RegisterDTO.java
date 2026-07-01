package com.gp_01.model.domain.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String phone;
    private String email;
}
