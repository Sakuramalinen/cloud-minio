package com.gp_01.auth.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RegisterType {

    PHONE_NUMBER_VERIFICATION_CODE(1,"手机验证码方式注册"),
    EMAIL_VERIFICATION_CODE(2, "邮箱方式注册");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String desc;

}
