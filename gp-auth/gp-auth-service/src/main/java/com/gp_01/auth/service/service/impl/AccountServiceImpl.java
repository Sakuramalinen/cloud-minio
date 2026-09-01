package com.gp_01.auth.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gp_01.auth.model.dto.LoginFormDTO;
import com.gp_01.auth.model.dto.RegisterDTO;
import com.gp_01.auth.model.po.Account;
import com.gp_01.auth.model.vo.LoginVO;
import com.gp_01.auth.service.config.AuthProperties;
import com.gp_01.auth.service.mapper.AccountMapper;
import com.gp_01.auth.service.service.IAccountService;
import com.gp_01.auth.service.strategy.login.LoginStrategy;
import com.gp_01.auth.service.strategy.login.LoginStrategyFactory;
import com.gp_01.auth.service.strategy.register.RegisterStrategy;
import com.gp_01.auth.service.strategy.register.RegisterStrategyFactory;
import com.gp_01.auth.encrypt_sdk.utils.EncryptUtils;
import com.gp_01.file.model.domain.dto.userFile.CreateRootDTO;
import com.gp_01.file.api.client.UserFileClient;
import com.gp_01.user.api.client.UserClient;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements IAccountService {



    private final AuthProperties authProperties;

    private final EncryptUtils encryptUtils;

    private final LoginStrategyFactory loginStrategyFactory;

    private final RegisterStrategyFactory registerStrategyFactory;
    
    private final UserFileClient userFileClient;


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
        PrivateKey privateKey = encryptUtils.readPrivateKey(encryptUtils.getPrivateKeys().get("login"));

        //默认过期时间
        long expire = 12L;
        TimeUnit timeUnit = TimeUnit.HOURS;

        //点击记住我后过期时间
        if (loginFormDTO.getRememberMe()) {
            expire = authProperties.getExpire();
            timeUnit = TimeUnit.DAYS;
        }

        token = encryptUtils.JwtEncrypt(claims,privateKey, expire, timeUnit);

        return new LoginVO(token, account.getUserId());
    }


    @Override
    public void register(RegisterDTO dto) {
        //TODO 校验验证码


        //注册
        RegisterStrategy registerStrategy = registerStrategyFactory.get(dto.getRegisterType());
        Account account = registerStrategy.register(dto);

        //存数据库
        super.save(account);


        //创建根目录
        userFileClient.createRoot(new CreateRootDTO(account.getUserId()));
    }


}
