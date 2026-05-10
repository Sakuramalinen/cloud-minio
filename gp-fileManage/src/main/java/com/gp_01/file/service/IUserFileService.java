package com.gp_01.file.service;

import com.gp_01.file.domain.po.UserFile;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 用户逻辑文件表 服务类
 * </p>
 *
 * @author employee_01
 * @since 2026-05-08
 */
public interface IUserFileService extends IService<UserFile> {

    void uploadFile(MultipartFile file, Long parentId, String md5Hex);

    void makeDir(Long parentId, String fileName);

    void reName(Long id, String fileName);

    void deleteById(Long id);

    List<UserFile> listFileByParentId(Long parentId);
}
