package com.gp_01.user.controller;


import com.gp_01.common.domain.Result;
import com.gp_01.model.domain.dto.RegisterDTO;
import com.gp_01.model.domain.po.User;
import com.gp_01.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 用户基本信息表 前端控制器
 * </p>
 *
 * @author shenyongqi
 * @since 2026-06-18
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理控制器", description = "")
public class UserController {

    private final IUserService userService;



    @PostMapping("register")
    @Operation(summary = "用户注册")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto){
        userService.register(dto);
        return Result.success();
    }

    @GetMapping("list")
    @Operation(summary = "获取用户列表")
    public Result<List<User>> list(){
        List<User> list = userService.list();
        return Result.success(list);
    }
    @GetMapping("get/phone")
    @Operation(summary = "根据手机号查询用户")
    public Result<User> getUserByPhone(@RequestParam @NotBlank String phone){
        User user = userService.getUserByPhone(phone);
        return Result.success(user);
    }
    @GetMapping("get/email")
    @Operation(summary = "根据邮箱查询用户")
    public Result<User> getUserByEmail(@RequestParam @NotBlank String email){
        User user = userService.getUserByEmail(email);
        return Result.success(user);
    }



}
