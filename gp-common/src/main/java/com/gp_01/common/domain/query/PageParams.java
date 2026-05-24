package com.gp_01.common.domain.query;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(name = "分页请求参数")
public class PageParams implements Serializable {
    public static final Long DEFAULT_PAGE_SIZE = 10L;
    public static final Long DEFAULT_PAGE_NO = 1L;


    @SchemaProperty(name = "每页大小")
    private Long pageSize = DEFAULT_PAGE_SIZE;

    @SchemaProperty(name = "页码")
    private Long pageNo = DEFAULT_PAGE_NO;

    @SchemaProperty(name = "是否升序")
    private Boolean isAsc = true;

    @SchemaProperty(name = "排序字段")
    private String sortBy;

    public <T> Page<T> toPage(){
        return new Page<>(this.pageNo,this.pageSize);
    }
    public <T> Page<T> toPage(OrderItem ... orderItems){
        Page<T> page = new Page<>(pageNo, pageSize);
        if(orderItems != null){
            for (OrderItem orderItem : orderItems) {
                page.addOrder(orderItem);
            }
        }
        //判断是否有前端指定的排序字段
        if (StringUtils.isNotEmpty(sortBy)) {
            OrderItem orderItem = new OrderItem();
            orderItem.setAsc(isAsc);
            orderItem.setColumn(sortBy);
            page.addOrder(orderItem);
        }
        return page;
    }

    public <T> Page<T> toPage(String defaultSortBy, Boolean isAsc){
        Page<T> page = new Page<>(pageNo, pageSize);
        if(StringUtils.isBlank(sortBy)){
            sortBy = defaultSortBy;
            isAsc = this.isAsc;
        }
        OrderItem orderItem = new OrderItem();
        orderItem.setAsc(isAsc);
        orderItem.setColumn(sortBy);
        page.addOrder(orderItem);

        return page;
    }

}
