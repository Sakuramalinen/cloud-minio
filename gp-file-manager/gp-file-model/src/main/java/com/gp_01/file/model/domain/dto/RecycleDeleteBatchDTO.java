package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

@Data
@Schema(name = "批量删除回收站文件参数")
public class RecycleDeleteBatchDTO {
    @SchemaProperty(name = "主键")
    private Long id;
    @SchemaProperty(name = "文件类型")
    private Long type;
}
