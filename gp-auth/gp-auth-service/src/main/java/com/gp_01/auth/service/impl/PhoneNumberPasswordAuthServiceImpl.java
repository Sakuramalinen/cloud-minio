package com.gp_01.auth.service.impl;

import com.gp_01.api.client.UserClient;
import com.gp_01.auth.service.AuthService;
import com.gp_01.common.domain.Result;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.model.domain.dto.LoginFormDTO;
import com.gp_01.model.domain.po.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("phone_number_password_auth_service")
@RequiredArgsConstructor
public class PhoneNumberPasswordAuthServiceImpl implements AuthService {

    private final UserClient userClient;

    private final PasswordEncoder passwordEncoder;


    @Override
    public User execute(LoginFormDTO loginFormDTO) {
        //TODO 校验验证码

        //获取用户信息
        Result<User> r = userClient.getUserByPhone(loginFormDTO.getPhone());
        if(r == null){
            throw new BadRequestException("账号或密码错误");
        }
        User user = r.getData();
        //匹配密码
        boolean success = passwordEncoder.matches(loginFormDTO.getPassword(), user.getPassword());
        if(!success){
            throw new BadRequestException("账号或密码错误");
        }
        //清除敏感信息
        user.setPassword(null);
        return user;
    }
}
