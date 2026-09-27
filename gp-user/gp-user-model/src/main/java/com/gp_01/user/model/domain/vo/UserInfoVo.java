package com.gp_01.user.model.domain.vo;

import com.gp_01.auth.model.enums.UserStatusEnum;
import com.gp_01.user.model.domain.po.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class UserInfoVo extends User {

    private String phoneNumber;
    private String email;
    private Integer status;

}
