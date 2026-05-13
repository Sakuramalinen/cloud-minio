package com.gp_01.file.service.impl;

import com.gp_01.common.exception.BadRequestException;
import com.gp_01.file.config.MinioConfig;
import com.gp_01.file.domain.po.FileBase;
import com.gp_01.file.mapper.FileBaseMapper;
import com.gp_01.file.service.IFileBaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gp_01.file.util.MinioUtils;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * <p>
 * 文件信息表 服务实现类
 * </p>
 *
 * @author employee_01
 * @since 2026-05-07
 */
@Service
@RequiredArgsConstructor
public class FileBaseServiceImpl extends ServiceImpl<FileBaseMapper, FileBase> implements IFileBaseService {

    private final MinioConfig minioConfig;
    private final MinioUtils minioUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    //TODO 定时扫描文件系统与数据库 清理垃圾
    public FileBase uploadFile(MultipartFile file, String md5Hex) {
        if (file == null) {
            throw new BadRequestException("文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isEmpty(originalFilename)) {
            throw new BadRequestException("文件名异常");
        }
        try {
            //查询该文件是否已经上传过
            FileBase one = lambdaQuery().eq(FileBase::getFileMd5, md5Hex).one();
            if (one != null) {
                //引用计数+1
                lambdaUpdate()
                        .eq(FileBase::getFileMd5, md5Hex)
                        .setSql("ref_count = ref_count + 1")
                        .update();
                return one;
            }
            //准备基础信息
            String dir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileSuffix = "";
            int i = originalFilename.lastIndexOf(".");
            if (i != -1) {
                fileSuffix = file.getOriginalFilename().substring(i);
            }
            String path = getPath(dir, md5Hex, fileSuffix);

            //写入文件系统minio
            //TODO 通过设计模式 实现零侵入改变存储方式
            minioUtils.uploadFile(file, path);
            //组装数据库信息
            FileBase fileBase = new FileBase();
            fileBase.setFileSize(file.getSize());
            fileBase.setContentType(file.getContentType());
            fileBase.setBucketName(minioConfig.getBucketName());
            fileBase.setObjectPath(path);
            fileBase.setFileMd5(md5Hex);
            fileBase.setRefCount(1);
            //写入数据库
            super.save(fileBase);
            return fileBase;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }

    }

    @Override
    public void subtractRefCount(List<Long> ids) {
        if (!ids.isEmpty()){
            Map<Long, Integer> map = new HashMap<>();
            for(Long id : ids){
                Integer cnt = map.getOrDefault(id, 0);
                map.put(id, cnt + 1);
            }
            for (Map.Entry<Long, Integer> entry : map.entrySet()) {

                super.lambdaUpdate()
                        .eq(FileBase::getId, entry.getKey())
                        .setSql("ref_count = ref_count - " + entry.getValue() )
                        .update();
            }
        }

    }


    private String getPath(String dir, String md5Hex, String fileSuffix) {
        return dir + "/" + md5Hex + fileSuffix;
    }
}
