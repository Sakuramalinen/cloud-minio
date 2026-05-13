package com.gp_01.authsdk_gateway.utils;

import com.gp_01.common.domain.dto.LoginUserDTO;
import com.gp_01.common.exception.UnauthorizedException;
import io.micrometer.common.util.StringUtils;

public class AuthUtil {

    //TODO 解析token
    public LoginUserDTO parseToken(String token){
        if(StringUtils.isEmpty(token)){
            throw new UnauthorizedException("登录信息为空");
        }
        LoginUserDTO res = new LoginUserDTO();
        Long userId = Long.parseLong(token);
        res.setUserId(userId);
        return res;
    }
}
