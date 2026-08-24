package com.gp_01.file.service.controller;


import com.gp_01.common.domain.Result;
import com.gp_01.file.model.domain.vo.ListHistoryAvatarVO;
import com.gp_01.file.service.service.IUserAvatarService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shenyongqi
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/user-avatar")
@RequiredArgsConstructor
public class UserAvatarController {

    private final IUserAvatarService userAvatarService;


    @PostMapping("upload")
    @Operation(summary = "上传头像", description = "返回预签名上传url")
    public Result<String> updateAvatar(@RequestBody @NotNull String filename){
        String url = userAvatarService.uploadAvatar(filename);
        return Result.success(url);
    }
    @PostMapping("persistence")
    @Operation(summary = "持久化头像", description = "上传头像后调用，保存到数据库")
    public Result<Long> persistenceAvatar(){
        Long id = userAvatarService.persistenceAvatar();
        return Result.success(id);
    }

    @GetMapping("preview")
    @Operation(summary = "获取头像url", description = "")
    public Result<String> previewAvatar(@NotNull Long id){
        String url = userAvatarService.previewAvatar(id);
        return Result.success(url);
    }

    @GetMapping("history")
    @Operation(summary = "查看当前用户历史头像", description = "获取预签名url")
    public Result<List<ListHistoryAvatarVO>> previewHistoryAvatarList(){
        List<ListHistoryAvatarVO> res = userAvatarService.previewHistoryAvatarList();
        return Result.success(res);
    }
}
