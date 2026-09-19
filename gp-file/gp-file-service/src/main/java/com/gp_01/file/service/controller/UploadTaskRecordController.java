package com.gp_01.file.service.controller;


import com.gp_01.common.domain.Result;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.file.model.domain.dto.UploadProgressSaveDTO;
import com.gp_01.file.model.domain.dto.taskRecord.CreateUploadTaskRecordDTO;
import com.gp_01.file.model.domain.dto.transfer.UpdateUploadProgressDto;
import com.gp_01.file.model.domain.po.UploadTaskRecord;
import com.gp_01.file.service.service.IUploadTaskRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "上传任务控制器",description = "废弃该功能")

public class UploadTaskRecordController {





}
