package com.gp_01.file.service.service;

import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.file.model.domain.dto.*;
import com.gp_01.file.model.domain.po.UserFile;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gp_01.file.model.domain.query.PageFilesQuery;
import com.gp_01.file.model.domain.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
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
    List<ListRecycleBinVO> listRecycleBin();

    /**
     * 从回收站恢复文件
     * @param ids
     */
    void restoreFile(List<Long> ids);
    
    PageResult<UserFile> listFileByTypeAndPage(PageParams params, Integer type);

    void moveFile(Long fileId, Long targetId);

    List<UserFile> listDirByParentId(Long id);

    /**
     * 批量删除回收站文件
     * @param ids
     */
    void deleteRecycleFileBatch(List<Long> ids);

}
