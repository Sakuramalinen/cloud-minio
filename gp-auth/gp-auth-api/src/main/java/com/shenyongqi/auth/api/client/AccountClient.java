package com.shenyongqi.auth.api.client;

import com.gp_01.auth.model.po.Account;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gp-auth-service")
public interface AccountClient {

    @GetMapping("account")
    Account getAccount(@RequestParam("userId") Long userId);
}
