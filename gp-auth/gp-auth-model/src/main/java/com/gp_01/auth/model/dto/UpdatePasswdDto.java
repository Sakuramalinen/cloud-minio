package com.gp_01.auth.model.dto;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePasswdDto {
    @NotNull
    @SchemaProperty(name = "修改方式，1用旧密码修改， 2用手机号验证码修改")
    private Integer type;

    @SchemaProperty(name = "旧密码")
    private String oldPasswd;

    @NotEmpty
    @SchemaProperty(name = "新密码")
    private String newPasswd;

    @SchemaProperty(name = "手机号")
    private String phoneNumber;

    @SchemaProperty(name = "验证码")
    private String code;

}
