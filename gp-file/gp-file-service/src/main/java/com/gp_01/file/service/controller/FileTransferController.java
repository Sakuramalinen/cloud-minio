package com.gp_01.file.service.controller;

import com.gp_01.common.domain.Result;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.file.model.domain.dto.*;
import com.gp_01.file.model.domain.vo.PreviewImagesVO;
import com.gp_01.file.model.domain.vo.UploadFileVO;
import com.gp_01.file.model.domain.vo.UploadPreSignVO;
import com.gp_01.file.service.service.IFileTransferService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("file-transfer")
@RequiredArgsConstructor
public class FileTransferController {

    private final IFileTransferService fileTransferService;


    @PostMapping("upload/auth")
    @Operation(summary = "文件上传授权", description = "上传文件前请求，判断是否具备资格，以及判断秒传")
    public Result<UploadFileVO> uploadAuthorize(@RequestBody @Valid UploadAuthorizationDTO dto){
        UploadFileVO vo = fileTransferService.uploadAuthorize(dto);
        return Result.success(vo);
    }

    @PutMapping("upload/pre-signed-url")
    @Operation(summary = "颁发上传预签名", description = "")
    public Result<UploadPreSignVO> uploadPreSign(@RequestBody UploadPreSignDTO dto){
        UploadPreSignVO vo = fileTransferService.getUploadPreSignedUrl(dto);

        return Result.success(vo);
    }

    @PostMapping("upload/complete")
    @Operation(summary = "上传成功", description = "分片合并，持久化")
    public Result<?> uploadComplete(@RequestBody UploadCompleteDTO dto){
        fileTransferService.uploadComplete(dto);
        return null;
    }


    @GetMapping("download/file")
    @Operation(summary = "下载文件", description = "获取OSS文件预签名url")
    public Result<String> downloadFile(@RequestParam @NotNull Long userFileId) {
        String url = fileTransferService.downloadFile(userFileId);
        return Result.success(url);
    }

    @GetMapping("preview/file")
    @Operation(summary = "原文件预览", description = "支持大文件分流预览")
    public Result<String> previewFile(@Valid @NotNull Long userFileId) {
        String url = fileTransferService.previewFile(userFileId);
        return Result.success(url);
    }

    @GetMapping("preview/images/page")
    @Operation(summary = "分页预览缩略图照片")
    public PageResult<PreviewImagesVO> previewThumbnailsPage(@Valid PageParams params) {

        return fileTransferService.previewThumbnailsPage(params);
    }




}
