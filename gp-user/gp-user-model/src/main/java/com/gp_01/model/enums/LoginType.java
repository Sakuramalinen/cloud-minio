package com.gp_01.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoginType {
    PHONE_NUMBER_PASSWORD(1,"手机号密码登陆","phone_number_password"),
    EMAIL_PASSWORD(2,"邮箱密码登陆","email_password"),
    PHONE_NUMBER_VERIFICATION_CODE(3, "手机号验证码登陆","phone_number_verification_code"),
    EMAIL_VERIFICATION_CODE(4, "邮箱验证码登陆", "email_verification_code"),
    WECHAT(5,"微信登陆", "wechat"),
    QQ(6, "QQ登陆", "QQ");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String desc;
    private final String BeanNamePrefix;

}
