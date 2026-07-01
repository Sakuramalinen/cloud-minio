package com.gp_01.file.service.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gp_01.file.model.domain.po.UserFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gp_01.file.model.domain.query.PageFilesQuery;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
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
     * 逻辑删除文件或文件夹下的所有文件
     * @param id
     * @param userId
     * @param timeStamp
     */
    void deleteFile(Long id, Long userId, Long timeStamp);

    /**
     * 查询父节点下的所有文件id，不包含文件夹
     * @param id
     * @return
     */
    ArrayList<Long> listFileIdByParentId(Long id, Long userId);

    /**
     * 批量查询文件夹下所有文件id
     * @param dirIds
     * @return
     */
    List<Long> listIdsByDirIds(List<Long> dirIds);

    Page<UserFile> listFileByPage(Page<UserFile> page, @Param("query")PageFilesQuery query, @Param("userId") Long userId);

    Integer existsSameFileName(Long id, Long userId, Long targetId);


}
