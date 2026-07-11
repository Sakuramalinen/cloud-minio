package com.gp_01.auth.controller;

import com.gp_01.auth.service.IFileService;
import com.gp_01.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class FileController {

    private final IFileService fileService;

    @GetMapping("auth/download/privilege/{id}")
    @Operation(summary = "获取下载凭证")
    public Result<String> getDownloadPrivilege(@PathVariable @NotNull Long id){

        String token = fileService.getDownloadPrivilege(id);
        return Result.success(token);
    }

}
