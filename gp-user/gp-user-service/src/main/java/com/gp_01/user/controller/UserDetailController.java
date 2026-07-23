package com.gp_01.user.controller;

import com.gp_01.common.domain.Result;
import com.gp_01.model.domain.po.UserDetail;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user-detail")
public class UserDetailController {


    @GetMapping
    @Operation(summary = "获取用户详细信息")
    public Result<UserDetail> getUserDetail(@RequestParam @NotNull Long userId){
        UserDetail userDetail = new UserDetail();
        userDetail.setVipLevel(0);
        return Result.success(userDetail);
    }
}
