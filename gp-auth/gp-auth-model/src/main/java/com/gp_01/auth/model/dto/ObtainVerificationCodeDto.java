package com.gp_01.auth.model.dto;

import com.gp_01.auth.model.enums.VerificationCodeType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "获取验证码dto")
public class ObtainVerificationCodeDto {

    @NotEmpty
    @SchemaProperty(name = "手机号")
    private String phoneNumber;

    @NotNull
    @SchemaProperty(name = "验证码类型")
    private VerificationCodeType codeType;
}
