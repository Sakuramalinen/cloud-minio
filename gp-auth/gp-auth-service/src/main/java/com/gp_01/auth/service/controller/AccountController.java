package com.gp_01.auth.service.controller;

import com.gp_01.auth.model.dto.*;
import com.gp_01.auth.model.enums.VerificationCodeType;
import com.gp_01.auth.model.po.Account;
import com.gp_01.auth.model.vo.LoginVO;
import com.gp_01.auth.service.service.IAccountService;

import com.gp_01.common.domain.Result;
import com.gp_01.common.enums.RequestHeaderEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


import org.springframework.web.bind.annotation.*;

import java.net.http.HttpRequest;

import static com.gp_01.common.enums.RequestHeaderEnum.LOGIN_AUTHORIZATION;

@RestController
@RequestMapping("account")
@RequiredArgsConstructor
@Tag(name = "权限管理控制器",description = "")
public class AccountController {


    private final IAccountService accountService;

    @PostMapping("login")
    @Operation(summary = "登陆并获取token")
    public Result<LoginVO> login(@RequestBody @Valid LoginFormDTO loginFormDTO){
        LoginVO vo = accountService.login(loginFormDTO);
        return Result.success(vo);
    }

    @PostMapping("logout")
    @Operation(summary = "退出登陆")
    public Result<Void> logout(@RequestHeader("Authorization") String token){
        accountService.logout(token);
        return Result.success();
    }


    @GetMapping("verificationCode")
    @Operation(summary = "发送手机验证码")
    public Result<Void> sendVerificationCode(String phoneNumber, VerificationCodeType codeType){
        accountService.sendVerificationCode(phoneNumber, codeType);
        return Result.success();
    }

//    @GetMapping("test")
//    public Result<Void> test(String phoneNumber, String code){
//        accountService.test(phoneNumber, code);
//        return Result.success();
//    }

    @PostMapping("register")
    @Operation(summary = "用户注册")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto){
        accountService.register(dto);
        return Result.success();
    }

    @PostMapping("passwd")
    @Operation(summary = "修改密码")
    public Result<Void> updatePasswd(@RequestBody UpdatePasswdDto dto){

        accountService.updatePasswd(dto);

        return Result.success();
    }
    @GetMapping
    @Operation(summary = "获取账户信息", description = "内部调用")
    public Account getAccount(Long userId){
        Account account = accountService.getById(userId);
        account.setPassword(null);
        return account;
    }

}
