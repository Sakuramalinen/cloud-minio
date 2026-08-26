package com.gp_01.file.service.controller;


import com.gp_01.common.domain.Result;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.file.model.domain.dto.*;
import com.gp_01.file.model.domain.po.UserFile;
import com.gp_01.file.model.domain.query.PageFilesQuery;
import com.gp_01.file.model.domain.vo.*;
import com.gp_01.file.service.service.IUserFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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



    @PostMapping("create/dir")
    @Operation(summary = "新建文件夹")
    public Result<Long> makeDir( @RequestBody @Valid MakeDirDTO dto) {
        Long id = userFileService.makeDir(dto);
        return Result.success(id);
    }

    @PostMapping("create/multi-dir")
    @Operation(summary = "创建层级文件夹")
    public Result<Long> makeMultiDir(@RequestBody @Valid MakeMultiDirDTO dto){
        Long dirId = userFileService.makeMultiDir(dto);
        return Result.success(dirId);
    }

    @PutMapping()
    @Operation(summary = "文件重命名")
    public Result<Void> fileReName(@RequestBody @Valid FileReNameDTO fileReNameDTO) {
        userFileService.reName(fileReNameDTO.getId(), fileReNameDTO.getFileName());
        return Result.success();
    }

    @GetMapping("list")
    @Operation(summary = "分页查询当前目录")
    public PageResult<UserFile> queryFilesByParentId(@Valid PageFilesQuery pageFilesQuery) {
        return userFileService.listFileByParentId(pageFilesQuery);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除文件或文件夹")
    public Result<Void> deleteById(@PathVariable @NotNull Long id) {
        userFileService.deleteById(id);
        return Result.success();
    }


    @GetMapping("recycle/page")
    @Operation(summary = "查看回收站")
    public PageResult<ListRecycleBinVO> recyclePage(PageParams params) {
        return userFileService.recyclePage(params);
    }

    @PutMapping("restore/batch")
    @Operation(summary = "从回收站恢复文件")
    public Result<Void> restoreRecycleFile(@RequestBody @NotEmpty List<Long> ids) {
        userFileService.restoreFile(ids);
        return Result.success();
    }

    @DeleteMapping("recycle/batch")
    @Operation(summary = "批量删除回收站文件")
    public Result<Void> deleteRecycleFileBatch(@RequestBody @NotEmpty List<Long> ids) {
        userFileService.deleteRecycleFileBatch(ids);
        return Result.success();
    }

    @PutMapping("move")
    @Operation(summary = "移动文件或文件夹")
    public Result<Void> moveFile(@RequestBody @Valid MoveFileDTO dto) {
        userFileService.moveFile(dto.getFileId(), dto.getParentId());
        return Result.success();
    }

    @GetMapping("dir/list/{parentId}")
    @Operation(summary = "查询文件夹目录")
    public Result<List<UserFile>> listDirByParentId(@PathVariable @NotNull Long parentId) {
        List<UserFile> res = userFileService.listDirByParentId(parentId);
        return Result.success(res);
    }

    @GetMapping("list/type/{file-type}")
    @Operation(summary = "根据文件类型分页查询")
    public PageResult<UserFile> listFileByTypeAndPage(@Valid PageParams params, @PathVariable("file-type") @NotNull Integer type) {
        return userFileService.listFileByTypeAndPage(params, type);
    }
    //TODO 未测试
    @GetMapping("copy")
    @Operation(summary = "复制文件或文件夹")
    public Result<Void> copyFile(CopyFileDTO dto){
        userFileService.copyFile(dto);
        return Result.success();
    }



}
