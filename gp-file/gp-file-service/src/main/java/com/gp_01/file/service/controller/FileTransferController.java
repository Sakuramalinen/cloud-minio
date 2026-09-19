package com.gp_01.file.service.controller;

import com.gp_01.common.domain.Result;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.file.model.domain.dto.*;
import com.gp_01.file.model.domain.dto.transfer.UpdateUploadProgressDto;
import com.gp_01.file.model.domain.po.FileObject;
import com.gp_01.file.model.domain.vo.PreviewImagesVO;
import com.gp_01.file.model.domain.vo.UploadFileVO;
import com.gp_01.file.model.domain.vo.UploadPreSignVO;
import com.gp_01.file.service.service.IFileTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("file-transfer")
@RequiredArgsConstructor
@Tag(name = "文件传输控制器",description = "")

public class FileTransferController {

    private final IFileTransferService fileTransferService;

    @GetMapping("instant")
    @Operation(summary = "判断秒传", description = "")
    public Result<FileObject> instantUpdate(String fileMd5){
        FileObject fileObject = fileTransferService.instantUpdate(fileMd5);
        return Result.success(fileObject);
    }

    @PostMapping("upload/auth")
    @Operation(summary = "文件上传授权", description = "上传文件前请求，判断是否具备资格")
    public Result<UploadFileVO> uploadAuthorize(@RequestBody @Valid UploadAuthorizationDTO dto){
        UploadFileVO vo = fileTransferService.uploadAuthorize(dto);
        return Result.success(vo);
    }

    @GetMapping("upload/progress")
    @Operation(summary = "获取上传进度", description = "断点续传")
    public Result<String> getChunkUploadProgress(){
        String bitMap = fileTransferService.getChunkUploadProgress();
        return Result.success(bitMap);
    }

    @PutMapping("upload/pre-signed-url")
    @Operation(summary = "颁发上传预签名", description = "获取上传url，分片上传，先请求获取该文件分片进度")
    public Result<UploadPreSignVO> uploadPreSign(@RequestBody UploadPreSignDTO dto){
        UploadPreSignVO vo = fileTransferService.getUploadPreSignedUrl(dto);
        return Result.success(vo);
    }

    @PostMapping("upload/progress")
    @Operation(summary = "更新上传进度", description = "用于断点续传")
    public Result<Void> setUploadProgress(@RequestBody UpdateUploadProgressDto dto){
        fileTransferService.setUploadProgress(dto.getChunkNumber());
        return Result.success();
    }

    @PostMapping("upload/complete")
    @Operation(summary = "上传成功", description = "分片合并，持久化")
    public Result<?> uploadComplete(@RequestBody UploadCompleteDTO dto){
        fileTransferService.uploadComplete(dto);
        return Result.success();
    }


    @GetMapping("download/file")
    @Operation(summary = "下载文件", description = "获取OSS文件预签名url")
    public Result<String> downloadFile(@RequestParam @NotNull Long userFileId) {
        String url = fileTransferService.downloadFile(userFileId);
        return Result.success(url);
    }

    @GetMapping("preview/file")
    @Operation(summary = "原文件预览", description = "支持大文件分流预览")
    public Result<String> previewFile(@Valid @NotNull Long fileId) {
        String url = fileTransferService.previewFile(fileId);
        return Result.success(url);
    }

    @GetMapping("preview/images/page")
    @Operation(summary = "分页预览缩略图照片")
    public PageResult<PreviewImagesVO> previewThumbnailsPage(@Valid PageParams params) {

        return fileTransferService.previewThumbnailsPage(params);
    }




}
