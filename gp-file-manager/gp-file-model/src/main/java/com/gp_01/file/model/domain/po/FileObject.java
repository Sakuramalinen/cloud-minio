package com.gp_01.file.model.domain.po;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * MinIO物理文件实体表
 * </p>
 *
 * @author shenyongqi
 * @since 2026-07-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("file_object")
@Schema(name="FileObject对象", description="物理文件实体表")
public class FileObject implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @SchemaProperty(name = "物理文件唯一ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @SchemaProperty(name = "冗余桶名，减少联表")
    private String bucketName;

    @SchemaProperty(name = "对象存储路径")
    private String objectPath;

    @SchemaProperty(name = "文件完整MD5，秒传匹配关键字段")
    private String fileMd5;

    @SchemaProperty(name = "MinIO上传返回ETag")
    private String eTag;

    @SchemaProperty(name = "文件大小 单位字节")
    private Long fileSize;

    @SchemaProperty(name = "文件MIME类型 image/jpeg、application/pdf等")
    private String contentType;

    @SchemaProperty(name = "文件后缀 jpg/mp4/docx")
    private String fileSuffix;

    @SchemaProperty(name = "引用计数：多少用户在使用该文件，计数为0时删除MinIO文件")
    private Integer refCount;

    @SchemaProperty(name = "首次上传该文件的用户ID")
    private Long uploadUserId;

    @SchemaProperty(name = "0表示未删除，非0表示删除时间戳")
    private Long isDeleted;

    @TableField(fill = FieldFill.INSERT)
    @SchemaProperty(name = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @SchemaProperty(name = "修改时间")
    private LocalDateTime updateTime;



}
