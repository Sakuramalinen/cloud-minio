package com.gp_01.file.service.service;

import com.gp_01.file.model.domain.po.FileBase;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gp_01.file.model.domain.po.UserFile;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 文件信息表 服务类
 * </p>
 *
 * @author employee_01
 * @since 2026-05-07
 */
public interface IFileBaseService extends IService<FileBase> {

    /**
     * 上传缩略图
     */
    void uploadThumbnailsFile(FileBase fileBase);

    /**
     * 获得文件存储url
     * @param objectPath
     * @param expireMinute
     * @return [源文件url，缩略图url]
     */
    String[] getTempSignedUrl(String objectPath, Integer expireMinute);

    /**
     * 根据md5判断文件是否存在于文件系统中
     *
     * @param identifier 文件唯一表示（md5）
     * @return
     */
    FileBase exist(String identifier);

    /**
     * 增加文件引用次数
     *
     * @param identifier 文件唯一表示（md5）
     */
    void incrementRefCount(String identifier);

    void incrementRefCount(Long id);

    void incrementRefCountBatch(List<Long> ids);

    void minusRefCount(String identifier);

    void minusRefCount(Long id);

    void minusRefCountBatch(List<Long> ids);

    /**
     * 获取完整存储路径
     *
     * @param createTime 文件创建时间
     * @param md5Hex 文件唯一表示（md5）
     * @param fileSuffix 文件扩展名 .xxx
     * @return
     */
    String getOriginalPath(LocalDateTime createTime, String md5Hex, String fileSuffix);

    String getOriginalPath(String objectPath);

    String getThumbnailsPath(LocalDateTime createTime, String md5Hex, String fileSuffix);

    String getThumbnailsPath(String objectPath);

    String getObjectPath(LocalDateTime createTime, String identifier, String extendName);
}
