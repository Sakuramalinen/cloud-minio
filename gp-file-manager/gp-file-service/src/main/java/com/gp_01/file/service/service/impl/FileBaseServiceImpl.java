package com.gp_01.file.service.service.impl;

import com.gp_01.common.exception.BadRequestException;
import com.gp_01.file.service.config.MinioConfig;
import com.gp_01.file.model.domain.po.FileBase;
import com.gp_01.file.model.domain.po.UserFile;
import com.gp_01.file.service.mapper.FileBaseMapper;
import com.gp_01.file.service.service.IFileBaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gp_01.file.service.util.ThumbnailUtils;
import com.gp_01.file.service.util.MinioUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.gp_01.file.service.constants.MinioConstants.ORIGINAL_BUCKET_NAME_PREFIX;
import static com.gp_01.file.service.constants.MinioConstants.THUMBNAIL_BUCKET_NAME_PREFIX;

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
    private final ThumbnailUtils thumbnailUtils;



    @Override
    public void uploadThumbnailsFile(FileBase fileBase) {
        //获得源文件输入流
        InputStream inputStream = minioUtils.downloadFile(getOriginalPath(fileBase.getObjectPath()));
        //转换成缩略图
        byte[] thumbnailBytes = thumbnailUtils.createThumbnailBytes(inputStream);
        if (thumbnailBytes == null) return;
        //拼接路径
        String path = getThumbnailsPath(fileBase.getObjectPath());
        //存到minio
        minioUtils.uploadOriginalFile(new ByteArrayInputStream(thumbnailBytes), path, fileBase.getContentType(), thumbnailBytes.length);
    }

    @Override
    public String getOriginalPath(String objectPath) {
        return ORIGINAL_BUCKET_NAME_PREFIX + "/" + objectPath;
    }

    @Override
    public String[] getTempSignedUrl(String objectPath, Integer expireMinute) {
        String[] res = new String[2];
        //分别获取路径
        String originalPath = getOriginalPath(objectPath);
        String thumbnailPath = getThumbnailsPath(objectPath);
        //获取url
        res[0] = minioUtils.getTempSignedUrl(originalPath, expireMinute);
        res[1] = minioUtils.getTempSignedUrl(thumbnailPath, expireMinute);

        return res;
    }



    @Override
    public String getOriginalPath(LocalDateTime createTime, String md5Hex, String fileSuffix) {

        return ORIGINAL_BUCKET_NAME_PREFIX + "/" + getObjectPath(createTime, md5Hex, fileSuffix);
    }

    @Override
    public String getObjectPath(LocalDateTime createTime, String identifier, String extendName) {
        String timePath = createTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return timePath + "/" + identifier + extendName;
    }

    @Override
    public String getThumbnailsPath(String objectPath) {
        return THUMBNAIL_BUCKET_NAME_PREFIX + "/" + objectPath;
    }

    public String getThumbnailsPath(LocalDateTime createTime, String md5Hex, String fileSuffix) {
        return THUMBNAIL_BUCKET_NAME_PREFIX + "/" + getObjectPath(createTime, md5Hex, fileSuffix);
    }

    private String getContentType(String contentType) {
        if (contentType.isEmpty()) return "";
        return contentType.split("/")[0];
    }

    @Override
    public FileBase exist(String identifier) {
        return lambdaQuery().eq(FileBase::getFileMd5, identifier).one();
    }

    @Override
    public void incrementRefCount(String identifier) {
        lambdaUpdate()
                .eq(FileBase::getFileMd5, identifier)
                .setSql("ref_count = ref_count + 1")
                .update();
    }

    @Override
    public void incrementRefCount(Long id) {
        lambdaUpdate()
                .eq(FileBase::getId, id)
                .setSql("ref_count = ref_count + 1")
                .update();
    }

    @Override
    public void incrementRefCountBatch(List<Long> ids) {
        lambdaUpdate()
                .in(FileBase::getId, ids)
                .setSql("ref_count = ref_count + 1")
                .update();
    }

    @Override
    public void minusRefCount(String identifier) {
        lambdaUpdate()
                .eq(FileBase::getFileMd5, identifier)
                .setSql("ref_count = ref_count - 1")
                .update();
    }

    @Override
    public void minusRefCount(Long id) {
        lambdaUpdate()
                .eq(FileBase::getId, id)
                .setSql("ref_count = ref_count - 1")
                .update();
    }

    @Override
    public void minusRefCountBatch(List<Long> ids) {
        lambdaUpdate()
                .in(FileBase::getId, ids)
                .setSql("ref_count = ref_count - 1")
                .update();
    }



}
