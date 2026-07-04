package com.gp_01.model.domain.dto;

import com.gp_01.model.enums.LoginType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "登陆表单")
public class LoginFormDTO{

    @NotNull
    @SchemaProperty(name = "登陆类型")
    private LoginType type;

    @SchemaProperty(name = "邮箱")
    private String email;

    @SchemaProperty(name = "手机号")
    private String phone;

    @NotBlank
    @SchemaProperty(name = "密码")
    private String password;

    @NotNull
    @SchemaProperty(name = "是否记住密码，七天免登陆")
    private Boolean rememberMe;
}
