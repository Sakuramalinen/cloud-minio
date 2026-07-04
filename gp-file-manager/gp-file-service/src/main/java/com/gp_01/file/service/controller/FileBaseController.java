package com.gp_01.file.service.controller;


import com.gp_01.file.service.service.IFileBaseService;
import com.gp_01.file.service.util.ThumbnailUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 文件信息表 前端控制器
 * </p>
 *
 * @author employee_01
 * @since 2026-05-07
 */
@RestController
@RequestMapping("/file-base")
@Tag(name = "物理文件管理控制器", description = "")
@RequiredArgsConstructor
public class FileBaseController {

    private final IFileBaseService fileBaseService;
    private final ThumbnailUtils thumbnailUtils;


//    @PostMapping("upload")
//    @Operation(summary = "文件上传")
//    public Result<FileBase> uploadFile(@RequestPart MultipartFile file) throws IOException {
////      TODO 后续有前端进行计算md5值
//        String md5Hex = DigestUtils.md5Hex(file.getInputStream());
//        FileBase fileBase = fileBaseService.uploadFile(file, md5Hex);
//        return Result.success(fileBase);
//    }

//    @GetMapping
//    public Result<String> getMD5(@RequestPart MultipartFile file) throws IOException {
//        String md5 = DigestUtils.md5Hex(file.getInputStream());
//        return Result.success(md5);
//    }


    @GetMapping("test")
    @Operation(summary = "test")
    public void test(MultipartFile file, HttpServletResponse response){

    }


}
