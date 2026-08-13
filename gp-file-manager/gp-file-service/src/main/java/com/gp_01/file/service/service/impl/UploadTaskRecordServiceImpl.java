package com.gp_01.file.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gp_01.common.context.UserContext;

import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.file.model.domain.dto.UploadProgressSaveDTO;
import com.gp_01.file.model.domain.po.UploadTaskRecord;
import com.gp_01.file.service.constants.RabbitmqFileConstants;
import com.gp_01.file.service.constants.RedisKeyFormatter;
import com.gp_01.file.service.mapper.UploadTaskRecordMapper;
import com.gp_01.file.service.service.IUploadTaskRecordService;
import com.gp_01.file.service.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 未完成的上传任务映射表 服务实现类
 * </p>
 *
 * @author shenyongqi
 * @since 2026-07-23
 */
@Service
@RequiredArgsConstructor
public class UploadTaskRecordServiceImpl extends ServiceImpl<UploadTaskRecordMapper, UploadTaskRecord> implements IUploadTaskRecordService {

    private final RedisUtils redisUtils;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public List<UploadTaskRecord> uploadProgressList() {
        Long userId = UserContext.getUser();

        List<UploadTaskRecord> list = super.lambdaQuery()
                .eq(UploadTaskRecord::getUserId, userId)
                .orderByDesc(UploadTaskRecord::getStatus)
                .list();

        List<String> keys = new ArrayList<>();
        for (UploadTaskRecord uploadTaskRecord : list) {
            String key = RedisKeyFormatter.chunkUploadProgressInfoKey(userId, uploadTaskRecord.getTaskId());
            keys.add(key);
        }
        //读取缓存中最新状态
        List<Map<String, String>> caches = redisUtils.getHashAllBatch(keys);
        for (int i = 0; i < list.size(); i++) {
            Map<String, String> cacheMap = caches.get(i);
            UploadTaskRecord uploadTaskRecord = list.get(i);
            if (cacheMap != null) {
                try {
                    if (cacheMap.get("status") != null) {
                        Integer status = Integer.parseInt(cacheMap.get("status"));
                        uploadTaskRecord.setStatus(status);
                    }
                    if (cacheMap.get("chunkBitmap") != null) {
                        String chunkBitmap = cacheMap.get("chunkBitmap");
                        uploadTaskRecord.setChunkBitmap(chunkBitmap);
                    }
                } catch (Exception e) {
                    log.error("查看上传进度获取缓存错误 -> {}", e);
                }
            }
        }


        return list;
    }

    @Override
    public void uploadProgressAsyncSave(UploadProgressSaveDTO dto) {
        Long userId = UserContext.getUser();
        //存缓存
        String key = RedisKeyFormatter.chunkUploadProgressInfoKey(userId, dto.getTaskId());
        HashMap<String, String> cacheMap = new HashMap<>();
        cacheMap.put("status", String.valueOf(dto.getStatus()));
        cacheMap.put("chunkBitmap", dto.getChunkBitMap());

        redisUtils.setHashAll(key, cacheMap, 10L, TimeUnit.MINUTES);

        //异步存数据库
        String exchange = RabbitmqFileConstants.EXCHANGE_TOPIC_FILE;
        String routingKey = RabbitmqFileConstants.RK_UPLOAD_PROGRESS_SAVE;

        rabbitTemplate.convertAndSend(exchange, routingKey, dto);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadProgressSaveBatch(UploadProgressSaveDTO dto) {
        //修改进度，状态
        List<UploadTaskRecord> list = new ArrayList<>();
        UploadTaskRecord uploadTaskRecord = new UploadTaskRecord()
                .setTaskId(dto.getTaskId())
                .setStatus(dto.getStatus())
                .setChunkBitmap(dto.getChunkBitMap());
        list.add(uploadTaskRecord);

        super.updateBatchById(list);
    }

    @Override
    public void uploadProgressDeleteBatch(List<Long> taskIds) {
        //查询每个任务是否有权限删除
        List<UploadTaskRecord> list = super.lambdaQuery()
                .in(UploadTaskRecord::getTaskId, taskIds)
                .list();
        Long userId = UserContext.getUser();

        for (UploadTaskRecord uploadTaskRecord : list) {
            if (!uploadTaskRecord.getUserId().equals(userId)) {
                throw new BadRequestException(ErrorCode.AUTHORITY_ERROR.getCode(), "有无权限删除的上传任务");
            }
        }
        //删除任务
        super.removeBatchByIds(taskIds);
    }
}
