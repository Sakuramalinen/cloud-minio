package com.gp_01.file.service.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gp_01.file.model.domain.po.FileSlice;
import com.gp_01.file.service.config.MinioConfig;
import com.gp_01.file.service.mapper.FileSliceMapper;
import com.gp_01.file.service.util.MinioUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
@RequiredArgsConstructor
public class minioTask {

    private final FileSliceMapper fileSliceMapper;

    private final MinioUtils minioUtils;

    private final MinioConfig minioConfig;

    /**
     * 清理上传中断的分片碎片
     * 执行一次最多清理10条
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearChunkFile(){
        LocalDateTime expireTime = LocalDateTime.now().minusDays(7);
        //TODO 得到重复uploadId
        LambdaQueryWrapper<FileSlice> wrapper = new LambdaQueryWrapper<FileSlice>().le(FileSlice::getCreateTime, expireTime);
        Map<String, FileSlice> sliceMap = fileSliceMapper.selectPage(new Page<>(1, 10), wrapper)
                .getRecords()
                .stream()
                .collect(Collectors.toMap(FileSlice::getUploadId, fileSlice -> fileSlice, (o,n) -> o));
        StringBuilder logs = new StringBuilder();
        logs.append("[");
        for (Map.Entry<String, FileSlice> entry : sliceMap.entrySet()) {
            //删除OSS中随拍呢
            minioUtils.abortInCompleteMultipartUpload(minioConfig.getBucketName(), entry.getValue().getObjectKey(), entry.getKey());
            logs.append(entry.getKey()).append(",");
        }
        if (logs.length() > 1) {
            logs.deleteCharAt(logs.length() - 1);
        }
        logs.append("]");
        //删除数据库中
        fileSliceMapper.delete(new LambdaQueryWrapper<FileSlice>().in(FileSlice::getUploadId, sliceMap.keySet()));

        log.debug("删除碎片成功 slices -> {}", logs);
    }
}
