package com.gp_01.file.service;

import com.gp_01.file.domain.po.FileBase;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gp_01.file.domain.po.UserFile;
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
     * 上传文件
     *
     * @param file
     * @param md5Hex
     * @return
     */
    FileBase uploadOriginalFile(MultipartFile file, String md5Hex);

    /**
     * 上传缩略图
     */
    void uploadThumbnailsFile(FileBase fileBase);

    /**
     * 减少文件计数
     *
     * @param ids
     */
    void subtractRefCount(List<Long> ids);

    //下载文件
    public void fileDownload(UserFile userFile, HttpServletResponse response);

    String getThumbnailsPath(String objectPath);

    String getOriginalPath(String objectPath);

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
     * @param identifier 文件唯一表示（md5）
     */
    void increment(String identifier);

    /**
     * 获取完整存储路径
     * @param createTime 文件创建时间
     * @param identifier 文件唯一表示（md5）
     * @param extendName 文件扩展名 .xxx
     * @return
     */
    String getIntegratePath(LocalDateTime createTime, String identifier, String extendName);


    String getObjectPath(LocalDateTime createTime, String identifier, String extendName);
}
