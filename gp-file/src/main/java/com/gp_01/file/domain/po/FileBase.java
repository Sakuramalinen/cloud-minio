package com.gp_01.file.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@Schema(name="FileBase对象", description="文件信息表")
public class FileBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @SchemaProperty(name = "主键(雪花算法)")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @SchemaProperty(name = "用户id")
    private Long userId;

    @SchemaProperty(name = "文件原始名")
    private String fileName;

    @SchemaProperty(name = "文件后缀")
    private String fileSuffix;

    @SchemaProperty(name = "文件大小字节")
    private Long fileSize;

    @SchemaProperty(name = "MIME类型")
    private String contentType;

    @SchemaProperty(name = "桶名")
    private String bucketName;

    @SchemaProperty(name = "存储路径")
    private String objectPath;

    @SchemaProperty(name = "文件MD5值")
    private String fileMd5;

    @SchemaProperty(name = "创建时间")
    private LocalDateTime createTime;

    @SchemaProperty(name = "逻辑删除 0未删除 1删除")
    private Integer deleted;


}
