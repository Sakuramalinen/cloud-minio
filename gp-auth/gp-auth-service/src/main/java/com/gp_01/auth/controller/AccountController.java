package com.gp_01.auth.controller;

import com.gp_01.api.client.UserClient;
import com.gp_01.auth.service.IAccountService;

import com.gp_01.common.domain.Result;
import com.gp_01.model.domain.dto.LoginFormDTO;
import com.gp_01.model.domain.po.User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;


import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("account")
@RequiredArgsConstructor
public class AccountController {
    private final UserClient userClient;


    private final IAccountService accountService;

    @PostMapping("login")
    @Operation(summary = "登陆并获取token")
    public Result<String> login(@RequestBody LoginFormDTO loginFormDTO){
        String token = accountService.login(loginFormDTO);
        return Result.success(token);
    }
    @PostMapping("logout")
    @Operation(summary = "退出登陆")
    public Result<Void> logout(){
        return Result.success();
    }
    @GetMapping("test")
    public Result<List<User>> Test(){
        return userClient.list();
    }


}
