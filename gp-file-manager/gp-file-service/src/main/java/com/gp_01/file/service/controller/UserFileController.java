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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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


    @PostMapping("upload/file")
    @Operation(summary = "上传文件", description = "支持断点续传")
    public Result<UploadVO> uploadFile(@RequestPart("file")
                                       @NotNull
                                       MultipartFile file,
                                       @ModelAttribute
                                       @Valid
                                       UploadFileDTO uploadFileDTO) {
        UploadVO vo = userFileService.upload(file, uploadFileDTO);
        return Result.success(vo);
    }



    @PostMapping("create/dir")
    @Operation(summary = "新建文件夹")
    public Result<Void> makeDir( @RequestBody @Valid MakeDirDTO dto) {
        userFileService.makeDir(dto);
        return Result.success();
    }
    @PostMapping("create/multi-dir")
    @Operation(summary = "创建层级文件夹")
    public Result<Long> makeMultiDir(@RequestBody @Valid MakeMultiDirDTO dto){
        Long dirId = userFileService.makeMultiDir(dto);
        return Result.success(dirId);
    }
    @PutMapping()
    @Operation(summary = "文件重命名")
    public Result<Void> reName(@RequestBody @Valid ReNameDTO reNameDTO) {
        userFileService.reName(reNameDTO.getId(), reNameDTO.getFileName());
        return Result.success();
    }



    @GetMapping("list")
    @Operation(summary = "分页查询当前目录")
    public PageResult<UserFile> queryFilesByParentId(@Valid PageFilesQuery pageFilesQuery) {
        return userFileService.listFileByParentId(pageFilesQuery);
    }



    @GetMapping("download/file")
    @Operation(summary = "下载文件", description = "支持大文件分片下载")
    public void downloadFile(HttpServletRequest request, HttpServletResponse response, @Valid DownloadFileDTO dto) {
        userFileService.downloadFile(request, response, dto);
    }


    @GetMapping("preview")
    @Operation(summary = "文件预览", description = "支持大文件分流预览")
    public void previewFile(HttpServletRequest request, HttpServletResponse response, @Valid PreviewFileDTO dto) {
        userFileService.previewFile(request, response, dto);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除文件或文件夹")
    public Result<Void> deleteById(@PathVariable @NotNull Long id) {
        userFileService.deleteById(id);
        return Result.success();
    }


    @GetMapping("recycle/list")
    @Operation(summary = "查看回收站")
    public Result<List<ListRecycleBinVO>> listRecycle() {
        List<ListRecycleBinVO> data = userFileService.listRecycleBin();
        return Result.success(data);
    }

    @PutMapping("restore")
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
        userFileService.moveFile(dto.getFileId(), dto.getTargetId());
        return Result.success();
    }

    @GetMapping("dir/list/{id}")
    @Operation(summary = "查询文件夹目录")
    public Result<List<UserFile>> listDirByParentId(@PathVariable @NotNull Long id) {
        List<UserFile> res = userFileService.listDirByParentId(id);
        return Result.success(res);
    }
    //TODO 文件分享功能

    @GetMapping("preview/images/list")
    @Operation(summary = "分页预览照片")
    public PageResult<PreviewImagesVO> listPreviewImagesByPage(@Valid PageParams params) {
        return userFileService.pagePreviewImages(params);
    }

    @GetMapping("list/type/{file-type}")
    @Operation(summary = "根据文件类型分页查询")
    public PageResult<UserFile> listFileByTypeAndPage(@Valid PageParams params, @PathVariable("file-type") @NotNull Integer type) {
        return userFileService.listFileByTypeAndPage(params, type);
    }

    @GetMapping("download/path/{id}")
    @Operation(summary = "获取文件下载路径")
    public Result<String> getDownloadPath(@PathVariable Long id){
        String path = userFileService.getDownloadPath(id);
        return Result.success(path);
    }


}
