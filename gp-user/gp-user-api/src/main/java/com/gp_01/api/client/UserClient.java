package com.gp_01.api.client;

import com.gp_01.common.domain.Result;
import com.gp_01.model.domain.po.User;
import com.gp_01.model.domain.po.UserDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gp-user-service")
public interface UserClient {


    @PostMapping("user/register")
    Result<Void> register(@RequestBody User user);

    @GetMapping("user/list")
    Result<List<User>> list();

    @GetMapping("user/get/phone")
    Result<User> getUserByPhone(@RequestParam String phone);

    @GetMapping("user/get/email")
    Result<User> getUserByEmail(@RequestParam String email);

    @GetMapping("user-detail")
    Result<UserDetail> getUserDetail(@RequestParam Long userId);
}
