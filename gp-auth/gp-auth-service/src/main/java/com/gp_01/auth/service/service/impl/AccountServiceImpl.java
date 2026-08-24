package com.gp_01.auth.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gp_01.auth.model.dto.LoginFormDTO;
import com.gp_01.auth.model.dto.RegisterDTO;
import com.gp_01.auth.model.po.Account;
import com.gp_01.auth.model.vo.LoginVO;
import com.gp_01.auth.service.config.JWTProperties;
import com.gp_01.auth.service.mapper.AccountMapper;
import com.gp_01.auth.service.service.IAccountService;
import com.gp_01.auth.service.strategy.login.LoginStrategy;
import com.gp_01.auth.service.strategy.login.LoginStrategyFactory;
import com.gp_01.auth.service.strategy.register.RegisterStrategy;
import com.gp_01.auth.service.strategy.register.RegisterStrategyFactory;
import com.gp_01.auth.service.utils.JWTUtils;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements IAccountService {



    private final JWTUtils jwtUtils;

    private final JWTProperties jwtProperties;

    private final LoginStrategyFactory loginStrategyFactory;

    private final RegisterStrategyFactory registerStrategyFactory;



    @Override
    public LoginVO login(LoginFormDTO loginFormDTO) {
        //TODO 校验验证码

        //登录
        LoginStrategy loginStrategy = loginStrategyFactory.get(loginFormDTO.getLoginType());
        Account account = loginStrategy.login(loginFormDTO);

        //生成token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", account.getUserId());
        String token;
        if(loginFormDTO.getRememberMe()){
            token =  jwtUtils.createToken(claims,jwtProperties.getExpire());
        }
        token =  jwtUtils.createToken(claims, 60 * 60 * 24L);
        return new LoginVO(token,account.getUserId());
    }


    @Override
    public void register(RegisterDTO dto) {
        //TODO 校验验证码


        //注册
        RegisterStrategy registerStrategy = registerStrategyFactory.get(dto.getRegisterType());
        Account account = registerStrategy.register(dto);

        //存数据库
        super.save(account);
    }


}
