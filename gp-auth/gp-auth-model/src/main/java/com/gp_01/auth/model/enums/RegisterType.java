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

    PHONE_NUMBER(1,"手机号码方式注册"),
    EMAIL(2, "邮箱方式注册");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String desc;

}
