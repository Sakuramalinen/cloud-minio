package com.gp_01.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RegisterType {

    PHONE_NUMBER(0,"手机号码方式注册", "phone_number"),
    EMAIL(1, "邮箱方式注册", "email");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String desc;
    private final String beanName;

    public static RegisterType getByValue(Integer val) {
        for (RegisterType registerType : RegisterType.values()) {
            if(registerType.value.equals(val)){
                return registerType;
            }
        }
        throw new BadRequestException(ErrorCode.PARAM_ERROR);
    }
}
