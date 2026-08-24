package com.gp_01.auth.model.dto;

import com.gp_01.auth.model.enums.RegisterType;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterDTO {

    @NotNull
    @SchemaProperty(name = "注册方式")
    private RegisterType registerType;

    @NotBlank
    @SchemaProperty(name = "密码")
    private String password;

    @SchemaProperty(name = "手机号")
    private String phone;

    @SchemaProperty(name = "邮箱")
    private String email;
}
