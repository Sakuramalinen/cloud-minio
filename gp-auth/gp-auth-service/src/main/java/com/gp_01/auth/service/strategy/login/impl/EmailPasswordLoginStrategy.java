package com.gp_01.auth.service.strategy.login.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gp_01.auth.model.dto.LoginFormDTO;
import com.gp_01.auth.model.enums.LoginType;
import com.gp_01.auth.model.po.Account;
import com.gp_01.auth.service.mapper.AccountMapper;
import com.gp_01.auth.service.strategy.login.LoginStrategy;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailPasswordLoginStrategy implements LoginStrategy {

    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;



    @Override
    public LoginType supportedType() {
        return LoginType.EMAIL_PASSWORD;
    }

    @Override
    public Account login(LoginFormDTO loginForm) {
        //TODO 校验验证码

        //获取用户信息
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<Account>().eq(Account::getEmail, loginForm.getEmail());
        Account account = accountMapper.selectOne(wrapper);
        if(account == null){
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "账号或密码错误");
        }

        //匹配密码
        boolean success = passwordEncoder.matches(loginForm.getPassword(), account.getPassword());
        if(!success){
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "账号或密码错误");
        }
        //清除敏感信息
        account.setPassword(null);
        return account;
    }
}
