package com.gp_01.auth.model.domain;

import lombok.Data;

@Data
public class PhonePasswordLoginForm {

    private String phone;
    private String password;
}
