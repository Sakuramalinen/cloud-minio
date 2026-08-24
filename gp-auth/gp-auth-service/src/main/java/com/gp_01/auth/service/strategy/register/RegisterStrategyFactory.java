package com.gp_01.auth.service.strategy.register;

import com.gp_01.auth.model.enums.RegisterType;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RegisterStrategyFactory {

    private final Map<RegisterType, RegisterStrategy> registers;


    public RegisterStrategyFactory(List<RegisterStrategy> list){
        this.registers = list.stream().collect(Collectors.toMap(RegisterStrategy::supportedType, r -> r));
    }

    public RegisterStrategy get(RegisterType type){
        RegisterStrategy registerStrategy = registers.get(type);
        if(registerStrategy == null){
            throw new BadRequestException(ErrorCode.PARAM_ERROR.getCode(), "不支持的注册方式");
        }
        return registerStrategy;
    }
}
