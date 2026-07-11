package com.gp_01.file.service.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gp_01.file.model.domain.po.UserFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gp_01.file.model.domain.query.PageFilesQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户逻辑文件表 Mapper 接口
 * </p>
 *
 * @author employee_01
 * @since 2026-05-08
 */
public interface UserFileMapper extends BaseMapper<UserFile> {


    /**
     * 批量查询回收站中文件夹下所有文件id
     * @param dirIds
     * @return id, fileId, fileType
     */
    List<UserFile> listByDirIds(@Param("dirIds") List<Long> dirIds);

    /**
     * 文件列表分页查询
     * @param page
     * @param query
     * @param userId
     * @return
     */
    Page<UserFile> listFileByPage(Page<UserFile> page, @Param("query")PageFilesQuery query, @Param("userId") Long userId);

    /**
     * 查询一个目录中是否有同名文件
     * @param id
     * @param userId
     * @param targetId
     * @return
     */
    Integer existsSameFileName(Long id, Long userId, Long targetId);


}
