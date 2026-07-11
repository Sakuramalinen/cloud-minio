package com.gp_01.file.model.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Schema(name = "创建文件夹接收参数")
@AllArgsConstructor
public class MakeDirDTO {
    @SchemaProperty(name = "父目录id")
    @NotNull
    private Long parentId;

    @SchemaProperty(name = "文件夹名称")
    @NotBlank
    private String fileName;

}
