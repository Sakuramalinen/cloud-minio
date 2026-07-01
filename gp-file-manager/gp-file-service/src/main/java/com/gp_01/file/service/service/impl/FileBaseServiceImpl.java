package com.gp_01.file.service.service.impl;

import com.gp_01.common.exception.BadRequestException;
import com.gp_01.file.service.config.MinioConfig;
import com.gp_01.file.model.domain.po.FileBase;
import com.gp_01.file.model.domain.po.UserFile;
import com.gp_01.file.service.mapper.FileBaseMapper;
import com.gp_01.file.service.service.IFileBaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gp_01.file.service.util.FileThumbnailUtils;
import com.gp_01.file.service.util.MinioUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.gp_01.file.service.constants.FileBaseConstants.FILE_DIR_FORMATTER;
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
    private final FileThumbnailUtils thumbnailUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    //TODO 定时扫描文件系统与数据库 清理垃圾
    public FileBase uploadOriginalFile(MultipartFile file, String md5Hex) {
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
            String dir = LocalDateTime.now().format(DateTimeFormatter.ofPattern(FILE_DIR_FORMATTER));
            String fileSuffix = "";
            int i = originalFilename.lastIndexOf(".");
            if (i != -1) {
                fileSuffix = file.getOriginalFilename().substring(i);
            }
            String OriginalPath = getOriginalPath(dir, md5Hex, fileSuffix);
            String objPath = getObjectPath(dir, md5Hex, fileSuffix);
            //写入文件系统minio
            //TODO 通过设计模式 实现零侵入改变存储方式
            minioUtils.uploadOriginalFile(file, OriginalPath);
            //组装数据库信息
            FileBase fileBase = new FileBase();
            fileBase.setFileSize(file.getSize());
            fileBase.setContentType(file.getContentType());
            fileBase.setBucketName(minioConfig.getBucketName());
            fileBase.setObjectPath(objPath);
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
    public void subtractRefCount(List<Long> ids) {
        if (!ids.isEmpty()) {
            Map<Long, Integer> map = new HashMap<>();
            for (Long id : ids) {
                Integer cnt = map.getOrDefault(id, 0);
                map.put(id, cnt + 1);
            }
            for (Map.Entry<Long, Integer> entry : map.entrySet()) {

                super.lambdaUpdate()
                        .eq(FileBase::getId, entry.getKey())
                        .setSql("ref_count = ref_count - " + entry.getValue())
                        .update();
            }
        }

    }

    @Override
    public void fileDownload(UserFile userFile, HttpServletResponse response) {
        if (userFile == null) {
            throw new BadRequestException("文件不存在");
        }
        //查询是否有该文件
        FileBase fileBase = super.getById(userFile.getFileId());
        if (fileBase == null) {
            log.error("file_base与user_file数据不一致");
            throw new BadRequestException("文件不存在");
        }
        //组装文件路径
        String path = getOriginalPath(fileBase.getObjectPath());
        //传输文件
        try (InputStream inputStream = minioUtils.downloadFile(path);
             BufferedInputStream bis = new BufferedInputStream(inputStream);
        ) {
            ServletOutputStream os = response.getOutputStream();
            byte[] buff = new byte[1024 * 1024 * 10];
            int len;
            while ((len = bis.read(buff)) != -1) {
                os.write(buff, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            log.error("下载文件失败：", e);
        }
    }
//    @Override
//    public void downloadFile(Long fileId, HttpServletRequest request, HttpServletResponse response, DownloadFileDTO downloadFileDTO, String fileName){
//        FileBase fileBase = null;
//        if (downloadFileDTO.getCurrentChunkIndex() == 1) {
//            //检查资源是否存在
//            fileBase = super.getById(fileId);
//        }
//        if(fileBase == null){
//            log.error("file_base与user_file表结构不一致");
//            throw new BadRequestException("资源不存在");
//        }
//
//
//    }

    @Override
    public String getThumbnailsPath(String objectPath) {
        return THUMBNAIL_BUCKET_NAME_PREFIX + "/" + objectPath;
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


    private String getObjectPath(String createTime, String md5Hex, String fileSuffix) {
        return createTime + "/" + md5Hex + fileSuffix;
    }

    private String getOriginalPath(String createTime, String md5Hex, String fileSuffix) {
        return ORIGINAL_BUCKET_NAME_PREFIX + "/" + getObjectPath(createTime, md5Hex, fileSuffix);
    }

    private String getThumbnailsPath(String createTime, String md5Hex, String fileSuffix) {
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


    @Override
    public String getIntegratePath(LocalDateTime createTime, String identifier, String extendName) {
        return ORIGINAL_BUCKET_NAME_PREFIX + "/" + getObjectPath(createTime, identifier, extendName);
    }

    public String getIntegratePath(String objectPath) {
        return ORIGINAL_BUCKET_NAME_PREFIX + "/" + objectPath;
    }

    @Override
    public String getObjectPath(LocalDateTime createTime, String identifier, String extendName) {
        String timePath = createTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return timePath + "/" + identifier + extendName;
    }
}
