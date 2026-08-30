package com.gp_01.file.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.file.model.domain.dto.UploadProgressSaveDTO;
import com.gp_01.file.model.domain.dto.taskRecord.CreateUploadTaskRecordDTO;
import com.gp_01.file.model.domain.po.UploadTaskRecord;

import java.util.List;

/**
 * <p>
 * 未完成的上传任务映射表 服务类
 * </p>
 *
 * @author shenyongqi
 * @since 2026-07-23
 */
public interface IUploadTaskRecordService extends IService<UploadTaskRecord> {

    List<UploadTaskRecord> uploadProgressList();

    /**
     * 异步修改上传进度
     * @param dto
     */
    void uploadProgressAsyncSave(UploadProgressSaveDTO dto);

    /**
     * 由mq异步调用批量修改上传进度
     * @param dtoList
     */
    void uploadProgressSaveBatch (UploadProgressSaveDTO dtoList);

    void uploadProgressDeleteBatch(List<Long> taskIds);

    UploadTaskRecord createUploadTaskRecord(CreateUploadTaskRecordDTO dto);
}
