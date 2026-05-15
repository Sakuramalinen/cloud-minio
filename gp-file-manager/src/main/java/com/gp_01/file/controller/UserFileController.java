package com.gp_01.file.controller;


import com.gp_01.common.domain.Result;
import com.gp_01.common.exception.ForbiddenException;
import com.gp_01.file.domain.dto.ReNameDTO;
import com.gp_01.file.domain.po.UserFile;
import com.gp_01.file.service.IUserFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 用户逻辑文件表 前端控制器
 * </p>
 *
 * @author employee_01
 * @since 2026-05-08
 */
@RestController
@RequestMapping("/user-file")
@Tag(name = "用户逻辑文件控制器", description = "")
@RequiredArgsConstructor
public class UserFileController {

    private final IUserFileService userFileService;

    @PutMapping("upload/file/{parentId}/{md5Hex}")
    @Operation(summary = "上传文件")
    public Result<Void> uploadFile(@RequestPart MultipartFile file, @PathVariable Long parentId, @PathVariable String md5Hex) {
        userFileService.uploadFile(file, parentId, md5Hex);
        return Result.success();
    }

    //TODO 上传文件夹，打成压缩包下载
    @PostMapping("upload/dir/")
    @Operation(summary = "上传文件夹")
    public Result<Void> uploadDir() {
        return null;
    }


    @PostMapping("create/{parentId}/{fileName}")
    @Operation(summary = "新建文件夹")
    public Result<Void> makeDir(@PathVariable Long parentId, @PathVariable String fileName) {
        userFileService.makeDir(parentId, fileName);
        return Result.success();
    }

    @PutMapping()
    @Operation(summary = "文件重命名")
    public Result<Void> reName(@RequestBody ReNameDTO reNameDTO) {
        userFileService.reName(reNameDTO.getId(), reNameDTO.getFileName());
        return Result.success();
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除文件或文件夹")
    public Result<Void> deleteById(@PathVariable Long id) {
        userFileService.deleteById(id);
        return Result.success();
    }

    @GetMapping("list/{parentId}")
    @Operation(summary = "查询当前目录")
    public Result<List<UserFile>> listFileByParentId(@PathVariable Long parentId) {
        List<UserFile> res = userFileService.listFileByParentId(parentId);
        return Result.success(res);
    }

    //TODO 下载文件


    //TODO 移动文件夹

    //TODO 移动文件夹 需要查询当前文件夹目录，不查询文件

    //TODO 文件分享功能


}
