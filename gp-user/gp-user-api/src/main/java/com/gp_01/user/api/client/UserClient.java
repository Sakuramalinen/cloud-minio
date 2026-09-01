package com.gp_01.user.api.client;

import com.gp_01.common.domain.Result;
import com.gp_01.user.model.domain.dto.UpdateUsedStoreSizeDTO;
import com.gp_01.user.model.domain.po.User;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gp-user-service")
public interface UserClient {

    @PostMapping("/user/create")
    @Operation(summary = "创建用户")
    Result<User> createUser();

    @GetMapping("/user/get")
    @Operation(summary = "获取用户信息")
    Result<User> getUserInfo(@RequestParam Long id);


    @PostMapping("/user/store/increment")
    @Operation(summary = "增加已使用存储空间", description = "上传文件时 文件服务调用")
     Result<Long> incrementUsedStoreSize(@RequestBody UpdateUsedStoreSizeDTO dto);



}
