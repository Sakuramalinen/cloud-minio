package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "创建层级文件夹接收参数")
public class MakeMultiDirDTO {

    @SchemaProperty(name = "父目录id")
    @NotNull
    private Long parentId;

    @SchemaProperty(name = "创建层级文件夹目录")
    @NotEmpty
    private String relativePath;
}
