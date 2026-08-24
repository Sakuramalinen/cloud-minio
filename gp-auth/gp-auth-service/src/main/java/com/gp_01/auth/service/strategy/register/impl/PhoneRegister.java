package com.gp_01.auth.service.strategy.register.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gp_01.user.api.client.UserClient;
import com.gp_01.auth.model.dto.RegisterDTO;
import com.gp_01.auth.model.enums.RegisterType;
import com.gp_01.auth.model.po.Account;
import com.gp_01.auth.service.mapper.AccountMapper;
import com.gp_01.auth.service.strategy.register.RegisterStrategy;
import com.gp_01.common.domain.Result;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.user.model.domain.po.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PhoneRegister implements RegisterStrategy {

    private final AccountMapper accountMapper;

    private final UserClient userClient;

    private final PasswordEncoder passwordEncoder;


    @Override
    public RegisterType supportedType() {
        return RegisterType.PHONE_NUMBER;
    }

    @Override
    public Account register(RegisterDTO dto) {

        //判断是否存在
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getPhone() != null, Account::getPhone, dto.getPhone());
        Account one = accountMapper.selectOne(wrapper);
        if (one != null) {
            log.debug("用户已存在");
            throw new BadRequestException(ErrorCode.USER_EXIST_ERROR);
        }
        Result<User> result = userClient.createUser();
        User user = result.getData();
        //密码加密
        String encodePassword = passwordEncoder.encode(dto.getPassword());
        return new Account()
                .setUserId(user.getId())
                .setPassword(encodePassword)
                .setPhone(dto.getPhone())
                .setDeleted(0L)
                .setStatus(0);
    }
}
