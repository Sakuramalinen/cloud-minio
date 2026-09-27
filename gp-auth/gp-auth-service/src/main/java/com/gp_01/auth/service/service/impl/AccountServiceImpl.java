package com.gp_01.auth.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gp_01.auth.model.dto.LoginFormDTO;
import com.gp_01.auth.model.dto.RegisterDTO;
import com.gp_01.auth.model.dto.UpdatePasswdDto;
import com.gp_01.auth.model.enums.VerificationCodeType;
import com.gp_01.auth.model.po.Account;
import com.gp_01.auth.model.vo.LoginVO;
import com.gp_01.auth.service.config.AuthProperties;
import com.gp_01.auth.service.constants.RedisKeyFormat;
import com.gp_01.auth.service.mapper.AccountMapper;
import com.gp_01.auth.service.sdk.MessageVerifier;
import com.gp_01.auth.service.service.IAccountService;
import com.gp_01.auth.service.strategy.login.LoginStrategy;
import com.gp_01.auth.service.strategy.login.LoginStrategyFactory;
import com.gp_01.auth.service.strategy.register.RegisterStrategy;
import com.gp_01.auth.service.strategy.register.RegisterStrategyFactory;
import com.gp_01.auth.encrypt_sdk.utils.EncryptUtils;
import com.gp_01.auth.service.utils.RedisUtils;
import com.gp_01.common.context.UserContext;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final PasswordEncoder passwordEncoder;

    private final LoginStrategyFactory loginStrategyFactory;

    private final RegisterStrategyFactory registerStrategyFactory;

    private final MessageVerifier messageVerifier;

    private final RedisUtils redisUtils;


    @Override
    public LoginVO login(LoginFormDTO loginFormDTO) {

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

        //注册
        RegisterStrategy registerStrategy = registerStrategyFactory.get(dto.getRegisterType());
        Account account = registerStrategy.register(dto);

        //存数据库
        super.save(account);

    }

    @Override
    public void sendVerificationCode(String phoneNumber, VerificationCodeType codeType) {
        String key = VerificationCodeType.getKey(codeType, phoneNumber);

        String s = redisUtils.get(key);
        if(s != null && !s.isEmpty()){
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "请勿连续发送验证码");
        }

        String code = messageVerifier.send(phoneNumber, codeType);

        redisUtils.set(key, code, 6L, TimeUnit.MINUTES);
    }

    @Override
    public void test(String phoneNumber, String code) {
        messageVerifier.check(phoneNumber, code);
    }

    @Override
    public void updatePasswd(UpdatePasswdDto dto) {


        Long userId = UserContext.getUser();
        String encode = passwordEncoder.encode(dto.getNewPasswd());
        Account account = super.getById(userId);
        //旧密码修改
        if (dto.getType() == 1) {

            if(!passwordEncoder.matches(dto.getOldPasswd(), account.getPassword())){
                throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "旧密码错误");
            }

            //修改密码
            super.lambdaUpdate().eq(Account::getUserId, userId).set(Account::getPassword, encode).update();
        }
        //手机号验证码修改
        if(dto.getType() == 2){
            //校验手机号码是否相同
            if(!account.getPassword().equals(dto.getPhoneNumber())){
                throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(),"手机号码不一致");
            }
            String key = VerificationCodeType.getKey(VerificationCodeType.UPDATE_PASSWD,dto.getPhoneNumber());
            String code = redisUtils.getDel(key);

            //校验验证码是否相同
            if(!code.equals(dto.getCode())){
                throw new BadRequestException(ErrorCode.VERIFICATION_CODE_ERROR);
            }

            //修改密码
            super.lambdaUpdate().eq(Account::getUserId, userId).set(Account::getPassword, encode).update();
        }

    }

    @Override
    public void logout(String token) {
        String logoutInfoKey = RedisKeyFormat.getLogoutInfoKey();
        long now = System.currentTimeMillis();
        long expire =  TimeUnit.DAYS.toMillis(7) + now;
        //加入黑名单
        redisUtils.addZSet(logoutInfoKey, token, (double) expire);
        //清理过期key
        redisUtils.clearRangeZSet(logoutInfoKey, 0L, now);
    }


}
