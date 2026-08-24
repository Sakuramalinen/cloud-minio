package com.gp_01.user.api.client;

import com.gp_01.common.domain.Result;
import com.gp_01.user.model.domain.po.User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("gp-user-service")
public interface UserClient {

    @PostMapping("/user/create")
    @Operation(summary = "创建用户")
    Result<User> createUser();

    @GetMapping("get")
    @Operation(summary = "获取用户信息")
    Result<User> getUserInfo(@NotNull Long id);


    @PostMapping("store/increment")
    @Operation(summary = "增加已使用存储空间", description = "上传文件时 文件服务调用")
     Result<Long> incrementUsedStoreSize(Long size);


    @PostMapping("store/minus")
    @Operation(summary = "减少已使用存储空间", description = "删除文件时 文件服务调用")
     Result<Long> minusUsedStoreSize(Long size);
}
