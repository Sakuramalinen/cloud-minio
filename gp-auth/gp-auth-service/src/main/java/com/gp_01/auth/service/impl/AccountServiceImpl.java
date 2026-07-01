package com.gp_01.auth.service.impl;

import com.gp_01.auth.service.AuthService;
import com.gp_01.auth.service.IAccountService;
import com.gp_01.auth.utils.JWTUtils;
import com.gp_01.model.domain.dto.LoginFormDTO;
import com.gp_01.model.domain.po.User;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements IAccountService {


    private final ApplicationContext applicationContext;

    private final JWTUtils jwtUtils;

    @Override
    public String login(LoginFormDTO loginFormDTO) {
        //TODO 校验验证码

        String beanName = loginFormDTO.getType().getBeanNamePrefix() + "_auth_service";
        AuthService authService = (AuthService) applicationContext.getBean(beanName);
        //登录
        User user = authService.execute(loginFormDTO);

        return jwtUtils.createUserToken(user);
    }
}
