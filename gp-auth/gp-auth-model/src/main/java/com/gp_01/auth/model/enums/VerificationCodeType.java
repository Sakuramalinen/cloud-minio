package com.gp_01.auth.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VerificationCodeType {

    LOGIN("100001","gp_01:auth-service:verification_code_phone_number:login:%s"),
    REGISTER("100001","gp_01:auth-service:verification_code_phone_number:register:%s"),
    UPDATE_PASSWD("100001","gp_01:auth-service:register:verification_code_phone_number:update_passwd:%s"),
    UPDATE_BIND_PHONE_NUMBER("100002","gp_01:auth-service:register:verification_code_phone_number:update_bind_phone_number:%s");

    @EnumValue
    @JsonValue
    private final String code;

    private final String keyFormat;


    public static String getKey(VerificationCodeType codeType, String s){
        return String.format(codeType.getKeyFormat(), s);
    }

}
