package com.gp_01.file.service.controller;


import com.gp_01.common.domain.Result;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.file.model.domain.dto.UploadProgressSaveDTO;
import com.gp_01.file.model.domain.po.UploadTaskRecord;
import com.gp_01.file.service.service.IUploadTaskRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 未完成的上传任务映射表 前端控制器
 * </p>
 *
 * @author shenyongqi
 * @since 2026-07-23
 */
@RestController
@RequestMapping("/upload-task-record")
@RequiredArgsConstructor
public class UploadTaskRecordController {

    private final IUploadTaskRecordService uploadTaskRecordService;

    @GetMapping("list")
    @Operation(summary = "分页查看该用户文件上传进度")
    public Result<List<UploadTaskRecord>> uploadProgressPage(){
        List<UploadTaskRecord> list = uploadTaskRecordService.uploadProgressList();
        return Result.success(list);
    }

    @PostMapping("save")
    @Operation(summary = "保存上传进度", description = "异步保存，先存缓存")
    public Result<?> uploadProgressSave(@RequestBody UploadProgressSaveDTO dto){
        uploadTaskRecordService.uploadProgressAsyncSave(dto);
        return Result.success();
    }
    @DeleteMapping("delete/batch")
    @Operation(summary = "删除上传任务")
    public Result<?> uploadProgressDeleteBatch(@RequestBody List<Long> taskIds){
        uploadTaskRecordService.uploadProgressDeleteBatch(taskIds);
        return Result.success();
    }




}
