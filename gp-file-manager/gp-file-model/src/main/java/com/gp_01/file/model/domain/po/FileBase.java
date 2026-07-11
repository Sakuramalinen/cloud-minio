package com.gp_01.file.model.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 文件信息表
 * </p>
 *
 * @author employee_01
 * @since 2026-05-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("file_base")
@Schema(name="FileBase对象", description="物理文件表")
public class FileBase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @SchemaProperty(name = "主键(雪花算法)")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;


    @SchemaProperty(name = "文件大小字节")
    private Long fileSize;

    @SchemaProperty(name = "MIME类型")
    private String contentType;

    @SchemaProperty(name = "桶名")
    private String bucketName;

    @SchemaProperty(name = "源文件存储路径")
    private String objectPath;

    @SchemaProperty(name = "文件MD5值")
    private String fileMd5;

    @SchemaProperty(name = "引用计数")
    private Integer refCount;

    @TableField(fill = FieldFill.INSERT)
    @SchemaProperty(name = "创建时间")
    private LocalDateTime createTime;




}
