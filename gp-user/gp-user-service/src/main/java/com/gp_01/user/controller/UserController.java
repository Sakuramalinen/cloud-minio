package com.gp_01.user.controller;


import com.gp_01.common.domain.Result;
import com.gp_01.user.model.domain.dto.UpdateUsedStoreSizeDTO;
import com.gp_01.user.model.domain.po.User;
import com.gp_01.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("get")
    @Operation(summary = "获取用户信息")
    public Result<User> getUserInfo(@NotNull @RequestParam("id") Long id){
        User user = userService.getUserInfo(id);
        return Result.success(user);
    }


    @PostMapping("update")
    @Operation(summary = "修改用户基本信息")
    public Result<Void> updateUserInfo(User user){
        userService.updateUserInfo(user);
        return Result.success();
    }

    @PostMapping("create")
    @Operation(summary = "创建用户", description = "注册时，权限服务内部调用")
    public Result<User> createUser(){
        User user = userService.createUser();
        return Result.success(user);
    }

    @PostMapping("store/increment")
    @Operation(summary = "增加已使用存储空间", description = "上传文件时 文件服务内部调用")
    public Result<Long> incrementUsedStoreSize(@RequestBody UpdateUsedStoreSizeDTO dto){
        userService.incrementUsedStoreSize(dto.getFileSize());
        return Result.success();
    }

    @PostMapping("store/minus")
    @Operation(summary = "减少已使用存储空间", description = "删除文件时 文件服务内部调用")
    public Result<Long> minusUsedStoreSize(UpdateUsedStoreSizeDTO dto){
        userService.minusUsedStoreSize(dto.getFileSize());
        return Result.success();
    }










}
