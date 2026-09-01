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
 * 未完成的上传任务映射表
 * </p>
 *
 * @author shenyongqi
 * @since 2026-07-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("upload_task_record")
@Schema(name="UploadTaskRecord对象", description="未完成的上传任务映射表")
public class UploadTaskRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @SchemaProperty(name = "任务流水号，主键")
    @TableId(value = "task_id", type = IdType.ASSIGN_ID)
    private Long taskId;

    @SchemaProperty(name = "父级目录")
    private Long parentId;

    @SchemaProperty(name = "文件的唯一标识(MD5)")
    private String fileMd5;

    @SchemaProperty(name = "文件名")
    private String fileName;

    @SchemaProperty(name = "分片上传id")
    private String uploadId;

    @SchemaProperty(name = "上传此文件的用户ID")
    private Long userId;

    @SchemaProperty(name = "上传状态 0-排队等待中, 1-已暂停, 2-网络中断")
    private Integer status;

    @SchemaProperty(name = "上传方式, 0-完整上传， 1-分片上传")
    private Boolean isChunked;

    @SchemaProperty(name = "每个分片大小")
    private Long chunkSize;

    @SchemaProperty(name = "文件大小")
    private Long fileSize;

    @SchemaProperty(name = "分片进度位图 01串")
    private String chunkBitmap;

    @SchemaProperty(name = "存储桶")
    private String bucketName;

    @SchemaProperty(name = "存储路径")
    private String objectPath;


    @TableField(fill = FieldFill.INSERT)
    @SchemaProperty(name = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @SchemaProperty(name = "修改时间")
    private LocalDateTime updateTime;

}
