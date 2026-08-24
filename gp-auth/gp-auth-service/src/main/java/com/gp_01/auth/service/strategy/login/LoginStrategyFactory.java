package com.gp_01.auth.service.strategy.login;

import com.gp_01.auth.model.enums.LoginType;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LoginStrategyFactory {

    private final Map<LoginType, LoginStrategy> strategies;

    public LoginStrategyFactory(List<LoginStrategy> list){
        this.strategies = list.stream()
                .collect(Collectors.toMap(LoginStrategy::supportedType, s -> s));
    }

    public LoginStrategy get(LoginType type){
        LoginStrategy loginStrategy = strategies.get(type);
        if(loginStrategy == null){
            throw new BadRequestException(ErrorCode.PARAM_ERROR.getCode(), "不支持的登录方式");
        }

        return loginStrategy;
    }


}
