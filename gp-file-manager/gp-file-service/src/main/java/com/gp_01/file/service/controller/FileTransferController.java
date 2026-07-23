package com.gp_01.file.service.controller;

import com.gp_01.common.domain.Result;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.file.model.domain.dto.PreviewFileDTO;
import com.gp_01.file.model.domain.dto.UploadAuthorizationDTO;
import com.gp_01.file.model.domain.dto.UploadChunkFileMergeDTO;
import com.gp_01.file.model.domain.dto.UploadChunkFileDTO;
import com.gp_01.file.model.domain.vo.PreviewImagesVO;
import com.gp_01.file.model.domain.vo.UploadFileVO;
import com.gp_01.file.service.service.IFileTransferService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    @PostMapping("upload/direction-connect/whole-file")
    @Operation(summary = "直连完整文件上传", description = "响应给前端预签名url，直连OSS进行文件完整上传，适用于小文件")
    public Result<String> directConnectionWholeUploadFile(@RequestBody @NotBlank String uploadId){
        String url = fileTransferService.directConnectionWholeUploadFile(uploadId);
        return Result.success(url);
    }

    @PostMapping("upload/direction-connect/chunk-file")
    @Operation(summary = "直连分片文件上传", description = "直连OSS进行文件分片上传，适用于大文件，支持断点续传")
    public Result<Map<Integer, String>> directConnectionChunkUploadFile(@RequestBody @Valid UploadChunkFileDTO dto){
        Map<Integer, String> urls = fileTransferService.directConnectionChunkUploadFile(dto.getUploadId(), dto.getChunkNumbers());
        return Result.success(urls);
    }

    @PostMapping("upload/merge")
    @Operation(summary = "分片文件合并", description = "分片文件上传完成，调用合并文件")
    public Result<Void> uploadChunkFileMerge(@RequestBody @Valid UploadChunkFileMergeDTO dto){
        fileTransferService.uploadChunkFileMerge(dto.getUploadId(), dto.getParts());
        return Result.success();
    }
    @PostMapping("upload/save")
    @Operation(summary = "保存上传文件", description = "直接完整上传后调用")
    public Result<Void> saveUploadFile(@RequestBody @NotBlank String uploadId){
        fileTransferService.saveUploadFile(uploadId);
        return Result.success();
    }

    @GetMapping("download/direction-connect/file")
    @Operation(summary = "直连下载", description = "获取OSS文件预签名url直连下载")
    public Result<String> directConnectionDownload(@RequestParam @NotNull Long id) {
        String url = fileTransferService.directionConnectionDownload(id);
        return Result.success(url);
    }

    @GetMapping("preview")
    @Operation(summary = "原文件预览", description = "支持大文件分流预览")
    public Result<String> previewFile(@Valid PreviewFileDTO dto) {
        String url = fileTransferService.directionConnectionPreview(dto);
        return Result.success(url);
    }

    @GetMapping("preview/images/page")
    @Operation(summary = "分页预览缩略图照片")
    public PageResult<PreviewImagesVO> previewThumbnailsPage(@Valid PageParams params) {

        return fileTransferService.previewThumbnailsPage(params);
    }


}
