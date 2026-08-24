package com.gp_01.file.model.domain.po;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author shenyongqi
 * @since 2026-08-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_avatar")
@Schema(name="UserAvatar对象", description="")
public class UserAvatar implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @SchemaProperty(name = "用户id")
    private Long userId;

    @SchemaProperty(name = "存储地址")
    private String objectPath;

    @SchemaProperty(name = "mime类型")
    private String contentType;

    @SchemaProperty(name = "文件大小")
    private Long fileSize;

    @SchemaProperty(name = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


}
