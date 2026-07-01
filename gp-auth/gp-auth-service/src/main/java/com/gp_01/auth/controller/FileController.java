package com.gp_01.auth.controller;

import com.gp_01.auth.service.IFileService;
import com.gp_01.common.domain.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class FileController {

    private final IFileService fileService;

    @GetMapping("auth/download/privilege/{id}")
    public Result<String> getDownloadPrivilege(@PathVariable Long id){

        String token = fileService.getDownloadPrivilege(id);

        return Result.success(token);
    }
}
