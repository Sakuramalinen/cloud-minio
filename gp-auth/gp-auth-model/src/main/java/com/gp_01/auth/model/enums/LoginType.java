package com.gp_01.auth.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoginType {
    PHONE_NUMBER_PASSWORD(1,"手机号密码登陆"),
    EMAIL_PASSWORD(2,"邮箱密码登陆"),
    PHONE_NUMBER_VERIFICATION_CODE(3, "手机号验证码登陆"),
    EMAIL_VERIFICATION_CODE(4, "邮箱验证码登陆"),
    WECHAT(5,"微信登陆"),
    QQ(6, "QQ登陆");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String desc;

}
