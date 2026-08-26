package com.gp_01.file.service.service;

import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.file.model.domain.dto.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gp_01.file.model.domain.po.UserFile;
import com.gp_01.file.model.domain.query.PageFilesQuery;
import com.gp_01.file.model.domain.vo.*;

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


    Long makeDir(MakeDirDTO dto);

    Long makeMultiDir(MakeMultiDirDTO dto);
    void reName(Long id, String fileName);

    void deleteById(Long id);

    PageResult<UserFile> listFileByParentId(PageFilesQuery pageFilesQuery);



    /**
     * 查看回收站
     * @return
     */
    PageResult<ListRecycleBinVO> recyclePage(PageParams params);

    /**
     * 从回收站恢复文件
     * @param ids
     */
    void restoreFile(List<Long> ids);
    
    PageResult<UserFile> listFileByTypeAndPage(PageParams params, Integer type);

    void moveFile(Long fileId, Long targetId);

    List<UserFile> listDirByParentId(Long parentId);

    /**
     * 批量删除回收站文件
     * @param ids
     */
    void deleteRecycleFileBatch(List<Long> ids);

    void copyFile(CopyFileDTO dto);
}
