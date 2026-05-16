package com.gp_01.file.mapper;

import com.gp_01.file.domain.po.UserFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.ArrayList;

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
}
