package com.gp_01.file.service;

import com.gp_01.file.domain.po.FileBase;
import com.baomidou.mybatisplus.extension.service.IService;
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

    FileBase uploadFile(MultipartFile file, String md5Hex);

    void subtractRefCount(List<Long> ids);
}
