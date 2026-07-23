package com.gp_01.api.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("gp-file-service")
public interface UserFileClient {

}
