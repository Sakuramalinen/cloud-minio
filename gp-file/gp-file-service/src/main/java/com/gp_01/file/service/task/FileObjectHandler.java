package com.gp_01.file.service.task;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gp_01.file.model.domain.po.FileObject;
import com.gp_01.file.service.oss.OSS;
import com.gp_01.file.service.service.IFileObjectService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileObjectHandler {

    private final IFileObjectService fileObjectService;

    private final OSS oss;


    /**
     * 清理没被引用的文件
     */
    @XxlJob("nullReferenceFileHandler")
    public void nullReferenceFileHandler(){
        log.debug(">>>>>>>>>>>定期清理文件存储服务");

        List<FileObject> records = fileObjectService.lambdaQuery()
                .eq(FileObject::getRefCount, 0)
                .page(new Page<>(0, 10)).getRecords();
        if(!records.isEmpty()){
            ArrayList<String> objectPaths = new ArrayList<>();
            for (FileObject record : records) {
                objectPaths.add(record.getObjectPath());
            }
            oss.fileManipulator.deleteObjectBatch(oss.getDefaultBucket(), objectPaths);
        }
    }
}
