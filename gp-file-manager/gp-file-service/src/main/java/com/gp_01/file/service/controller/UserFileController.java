package com.gp_01.file.service.controller;


import com.gp_01.common.domain.Result;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.file.model.domain.dto.*;
import com.gp_01.file.model.domain.po.UserFile;
import com.gp_01.file.model.domain.query.PageFilesQuery;
import com.gp_01.file.model.domain.vo.FileDetail;
import com.gp_01.file.model.domain.vo.ListRecycleBinVO;
import com.gp_01.file.model.domain.vo.PreviewImagesVO;
import com.gp_01.file.model.domain.vo.UploadVO;
import com.gp_01.file.service.service.IUserFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

//    @PutMapping("upload/file/{parentId}/{md5Hex}")
//    @Operation(summary = "上传文件")
    @Deprecated
    public Result<Void> uploadFile(@RequestPart MultipartFile file, @PathVariable Long parentId, @PathVariable String md5Hex) {
        userFileService.uploadFile(file, parentId, md5Hex);
        return Result.success();
    }
    @PostMapping("upload/file")
    @Operation(summary = "上传文件",description = "支持断点续传")
    public Result<UploadVO> uploadFile(@RequestPart("file") MultipartFile file, @ModelAttribute UploadFileDTO uploadFileDTO){
        UploadVO vo = userFileService.uploadFile(file, uploadFileDTO);
        return Result.success(vo);
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
    @GetMapping("download/file")
    @Operation(summary = "下载文件")
    public void downloadFile(HttpServletRequest request, HttpServletResponse response, DownloadFileDTO dto){
        userFileService.downloadFile(request, response, dto);
    }



//    @GetMapping("preview/{id}")
    @Operation(summary = "文件预览")
    public void previewFileById(@PathVariable String id, HttpServletResponse response){
        userFileService.previewFileById(id, response);
    }

    @GetMapping("preview")
    @Operation(summary = "文件预览", description = "支持大文件预览")
    public void previewFile(HttpServletRequest request, HttpServletResponse response, PreviewFileDTO dto){
        userFileService.previewFile(request, response, dto);
    }


    @GetMapping("recycle/list")
    @Operation(summary = "查看回收站")
    public Result<List<ListRecycleBinVO>> listRecycleBin(){
        List<ListRecycleBinVO> data = userFileService.listRecycleBin();
        return Result.success(data);
    }
    @PutMapping("restore")
    @Operation(summary = "从回收站恢复文件")
    public Result<Void> restoreFile(@RequestBody List<Long> ids){
        userFileService.restoreFile(ids);
        return Result.success();
    }
    @DeleteMapping("recycle/batch")
    public Result<Void> deleteRecycleFileBatch(@RequestBody List<Long> ids){
        userFileService.deleteRecycleFileBatch(ids);
        return Result.success();
    }

    @PutMapping("move")
    @Operation(summary = "移动文件或文件夹")
    public Result<Void> moveFile(@RequestBody MoveFileDTO dto){
        userFileService.moveFile(dto.getFileId(), dto.getTargetId());
        return Result.success();
    }
    @GetMapping("dir/list/{id}")
    @Operation(summary = "查询文件夹目录")
    public Result<List<UserFile>> listDirByParentId(@PathVariable Long id){
        List<UserFile> res = userFileService.listDirByParentId(id);
        return Result.success(res);
    }
    //TODO 文件分享功能

    @GetMapping("preview/images/list")
    @Operation(summary = "分页预览照片")
    public PageResult<PreviewImagesVO> listPreviewImagesByPage(PageParams params){
       return userFileService.pagePreviewImages(params);
    }
    @GetMapping("list/type/{file-type}")
    @Operation(summary = "根据文件类型分页查询")
    public PageResult<UserFile> listFileByTypeAndPage(PageParams params, @PathVariable("file-type") Integer type){
        return userFileService.listFileByTypeAndPage(params,type);
    }

    @GetMapping("download/detail/{id}")
    public Result<FileDetail> getFileDetail(@PathVariable Long id){
        FileDetail fileDetail = userFileService.getFileDetail(id);
        return Result.success(fileDetail);
    }


}
