package com.gp_01.file.service.mapper;

import com.gp_01.file.model.domain.po.FileBase;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * 文件信息表 Mapper 接口
 * </p>
 *
 * @author employee_01
 * @since 2026-05-07
 */
public interface FileBaseMapper extends BaseMapper<FileBase> {

    @Update("update file_base set ref_count = ref_count + 1 where file_md5 = #{fileMd5}")
    void incrementRefCount(String fileMd5);

    @Update("update file_base set ref_count = ref_count - 1 where file_md5 = #{fileMd5}")
    void minusRefCount(String fileMd5);

    void minusRefCountBatch(@Param("ids") List<Long> ids);
}
