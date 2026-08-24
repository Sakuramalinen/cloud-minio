package com.gp_01.auth.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
@AllArgsConstructor
@Getter
public enum UserStatusEnum {

    NORMAL(1,"正常"),
    BAN(2,"封禁中"),
    FREEZE(3, "冻结中");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String description;

}
