package com.gp_01.file.model.domain.po;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.time.LocalDateTime;

import java.io.Serializable;

import com.gp_01.common.enums.FileTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户逻辑目录表
 * </p>
 *
 * @author shenyongqi
 * @since 2026-07-19
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_file")
@Schema(name = "UserFile对象", description = "用户逻辑目录表")
public class UserFile implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @SchemaProperty(name = "用户文件记录ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @SchemaProperty(name = "归属用户ID")
    private Long userId;

    @SchemaProperty(name = "父级目录ID，0代表根目录")
    private Long parentId;

    @SchemaProperty(name = "关联物理文件ID，文件夹此字段为null")
    private Long objectId;

    @SchemaProperty(name = "用户自定义文件名/文件夹名")
    private String fileName;

    @SchemaProperty(name = "文件大小(冗余字段)")
    private Long fileSize;

    @SchemaProperty(name = "条目类型 1=文件夹 0=实体文件")
    private Integer isDirectory;

    @SchemaProperty(name = "文件细分类型：1视频 2音频 3图片 4文本 5其他，文件夹为null")
    private FileTypeEnum mediaCategory;

    @SchemaProperty(name = "排序权重，数值越大展示越靠前")
    private Integer sort;

    @SchemaProperty(name = "0表示未删除，非0表示删除时间戳")
    private Long deleted;

    @SchemaProperty(name = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @SchemaProperty(name = "修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
