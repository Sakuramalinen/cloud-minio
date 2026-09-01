package com.gp_01.file.api.client;

import com.gp_01.common.domain.Result;
import com.gp_01.file.model.domain.dto.userFile.CreateRootDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gp-file-service")
public interface UserFileClient {


    @PostMapping("user-file/create/root")
    @Operation(summary = "创建根目录")
    Result<Long> createRoot(@RequestBody CreateRootDTO dto);

}
