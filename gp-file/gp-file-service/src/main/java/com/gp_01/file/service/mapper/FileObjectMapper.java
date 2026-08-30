package com.gp_01.file.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gp_01.file.model.domain.po.FileObject;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * MinIO物理文件实体表 Mapper 接口
 * </p>
 *
 * @author shenyongqi
 * @since 2026-07-19
 */
public interface FileObjectMapper extends BaseMapper<FileObject> {


    @Update("update file_object set ref_count = ref_count + 1 where file_md5 = #{fileMd5}")
    void incrementRefCount(String fileMd5);

    @Update("update file_object set ref_count = ref_count - 1 where file_md5 = #{fileMd5}")
    void minusRefCount(String fileMd5);

    void minusRefCountBatch(@Param("ids") List<Long> ids);
}
