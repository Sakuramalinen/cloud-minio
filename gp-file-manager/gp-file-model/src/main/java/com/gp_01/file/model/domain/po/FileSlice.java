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
 * 文件分片上传临时表
 * </p>
 *
 * @author shenyongqi
 * @since 2026-07-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("file_slice")
@Schema(name="FileSlice对象", description="文件分片上传临时表")
public class FileSlice implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @SchemaProperty(name = "上传用户")
    private Long userId;

    @SchemaProperty(name = "分片存储桶")
    private String bucketName;

    @SchemaProperty(name = "MinIO分片上传唯一uploadId")
    private String uploadId;

    @SchemaProperty(name = "目标文件对象key")
    private String objectKey;

    @SchemaProperty(name = "分片序号")
    private Integer sliceIndex;

    @SchemaProperty(name = "当前分片上传后MinIO返回分片ETag")
    private String sliceETag;

    @SchemaProperty(name = "当前分片大小")
    private Long sliceSize;

    @SchemaProperty(name = "文件总大小")
    private Long totalSize;

    @SchemaProperty(name = "完整文件md5")
    private String fileMd5;

    @SchemaProperty(name = "分片上传过期时间，超时自动清理")
    private LocalDateTime expireTime;

    @TableField(fill = FieldFill.INSERT)
    @SchemaProperty(name = "创建时间")
    private LocalDateTime createTime;

    @SchemaProperty(name = "0表示未删除，非0表示删除时间戳")
    private Long isDeleted;


}
