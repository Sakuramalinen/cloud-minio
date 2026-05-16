package com.gp_01.file.service;

import com.gp_01.file.domain.po.FileBase;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gp_01.file.domain.po.UserFile;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
     * @param file
     * @param md5Hex
     * @return
     */
    FileBase uploadFile(MultipartFile file, String md5Hex);

    /**
     * 减少文件计数
     * @param ids
     */
    void subtractRefCount(List<Long> ids);

    //下载文件
    public void fileDownload(UserFile userFile, HttpServletResponse response);
}
