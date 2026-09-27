package com.gp_01.auth.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gp_01.auth.model.dto.AccountBindDto;
import com.gp_01.auth.model.dto.LoginFormDTO;
import com.gp_01.auth.model.dto.RegisterDTO;
import com.gp_01.auth.model.dto.UpdatePasswdDto;
import com.gp_01.auth.model.enums.VerificationCodeType;
import com.gp_01.auth.model.po.Account;
import com.gp_01.auth.model.vo.LoginVO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public interface IAccountService extends IService<Account> {
    LoginVO login(LoginFormDTO loginFormDTO);

    void register(RegisterDTO registerDTO);

    void sendVerificationCode(@NotEmpty String phoneNumber, @NotNull VerificationCodeType codeType);

    void test(String phoneNumber, String code);

    void updatePasswd(UpdatePasswdDto dto);

    void logout(String token);


}
