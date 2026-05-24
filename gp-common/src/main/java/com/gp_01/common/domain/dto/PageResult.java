package com.gp_01.common.domain.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "分页结果")
public class PageResult<T> implements Serializable {

    @SchemaProperty(name = "总条数")
    private Long total;
    @SchemaProperty(name = "每页大小")
    private Long pageSize;
    @SchemaProperty(name = "当前页码")
    private Long pageNo;
    @SchemaProperty(name = "数据列表")
    private List<T> items;

    public static <T> PageResult<T> empty(Page<?> page){
        return new PageResult<>(page.getTotal(), page.getSize(), page.getCurrent(), List.of());
    }

    public static <T> PageResult<T> empty(){
        return new PageResult<>(0L,0L,0L, List.of());
    }

    public static <T> PageResult<T> of(Page<T> page){
        if(page == null){
            return new PageResult<>();
        }
        if(page.getRecords().isEmpty()){
            return empty(page);
        }
        return new PageResult<>(page.getTotal(), page.getSize(), page.getCurrent(), page.getRecords());

    }

}
