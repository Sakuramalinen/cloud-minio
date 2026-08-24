package com.gp_01.auth.service.controller;

import com.gp_01.auth.model.dto.LoginFormDTO;
import com.gp_01.auth.model.dto.RegisterDTO;
import com.gp_01.auth.model.vo.LoginVO;
import com.gp_01.auth.service.service.IAccountService;

import com.gp_01.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("account")
@RequiredArgsConstructor
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
    public Result<Void> logout(){
        return Result.success();
    }


    @PostMapping("register")
    @Operation(summary = "用户注册")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto){
        accountService.register(dto);
        return Result.success();
    }

}
