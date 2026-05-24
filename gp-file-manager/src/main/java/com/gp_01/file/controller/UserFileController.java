package com.gp_01.file.controller;


import com.gp_01.common.domain.Result;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.file.domain.dto.ReNameDTO;
import com.gp_01.file.domain.po.UserFile;
import com.gp_01.file.domain.query.PageFilesQuery;
import com.gp_01.file.domain.vo.ListRecycleBinVO;
import com.gp_01.file.service.IUserFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
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
    private final DataSource dataSource;

    @PutMapping("upload/file/{parentId}/{md5Hex}")
    @Operation(summary = "上传文件")
    public Result<Void> uploadFile(@RequestPart MultipartFile file, @PathVariable Long parentId, @PathVariable String md5Hex) {
        userFileService.uploadFile(file, parentId, md5Hex);
        return Result.success();
    }

    //TODO 上传文件夹，
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

    @GetMapping("list")
    @Operation(summary = "分页查询当前目录")
    public PageResult<UserFile> queryFilesByParentId(PageFilesQuery pageFilesQuery) {
        return userFileService.listFileByParentId(pageFilesQuery);
    }

    @GetMapping("download/{id}")
    @Operation(summary = "下载单个文件")
    public void downloadById(@PathVariable Long id, HttpServletResponse response){
        userFileService.downloadById(id, response);
    }

    //预览文件
    @GetMapping("preview/{id}")
    @Operation(summary = "文件预览")
    public void previewFileById(@PathVariable String id, HttpServletResponse response){
        userFileService.previewFileById(id, response);
    }

    //TODO 查看回收站
    @GetMapping("recycle/list")
    @Operation(summary = "查看回收站")
    public Result<List<ListRecycleBinVO>> listRecycleBin(){
        List<ListRecycleBinVO> data = userFileService.listRecycleBin();
        return Result.success(data);
    }
    //TODO 将回收站文件放回原处
    @PutMapping("restore")
    public Result<Void> restoreFile(@RequestBody List<Long> ids){
        userFileService.restoreFile(ids);
        return Result.success();
    }
    //TODO 彻底删除回收站

    //TODO 移动文件夹

    //TODO 移动文件夹 需要查询当前文件夹目录，不查询文件

    //TODO 文件分享功能





    //TODO 彻底删除回收站文件




}
