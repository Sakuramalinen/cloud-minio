package com.gp_01.file.service.controller;

import com.gp_01.common.domain.Result;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.file.model.domain.dto.DownloadFileDTO;
import com.gp_01.file.model.domain.dto.PreviewFileDTO;
import com.gp_01.file.model.domain.dto.UploadFileDTO;
import com.gp_01.file.model.domain.vo.DownloadInfoVO;
import com.gp_01.file.model.domain.vo.PreviewImagesVO;
import com.gp_01.file.model.domain.vo.UploadVO;
import com.gp_01.file.service.service.IFileTransferService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("file-transfer")
@RequiredArgsConstructor
public class FileTransferController {

    private final IFileTransferService fileTransferService;

    @PostMapping("upload/file")
    @Operation(summary = "上传文件", description = "支持断点续传")
    public Result<UploadVO> uploadFile(@RequestPart("file")
                                       @NotNull
                                       MultipartFile file,
                                       @ModelAttribute
                                       @Valid
                                       UploadFileDTO uploadFileDTO) {
        UploadVO vo = fileTransferService.uploadFile(file, uploadFileDTO);
        return Result.success(vo);
    }

    @GetMapping("download/info/{id}")
    @Operation(summary = "获取文件下载信息", description = "下载文件前的请求，获取下载路径以及判断权限")
    public Result<DownloadInfoVO> getDownloadInfo(@PathVariable Long id){
        DownloadInfoVO downloadInfoVO = fileTransferService.getDownloadInfo(id);
        return Result.success(downloadInfoVO);
    }

    @GetMapping("download/file")
    @Operation(summary = "下载文件", description = "支持大文件分片下载")
    public void downloadFile(HttpServletRequest request, HttpServletResponse response, @Valid DownloadFileDTO dto) {
        fileTransferService.downloadFile(request, response, dto);
    }

    @GetMapping("preview")
    @Operation(summary = "文件预览", description = "支持大文件分流预览")
    public void previewFile(HttpServletRequest request, HttpServletResponse response, @Valid PreviewFileDTO dto) {
        fileTransferService.previewFile(request, response, dto);
    }

    @GetMapping("preview/images/page")
    @Operation(summary = "分页预览照片")
    public PageResult<PreviewImagesVO> listPreviewImagesByPage(@Valid PageParams params) {
        return fileTransferService.pagePreviewImages(params);
    }



}
