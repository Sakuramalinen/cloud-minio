package com.gp_01.auth.model.po;

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
 * 用户基本信息表
 * </p>
 *
 * @author shenyongqi
 * @since 2026-08-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("account")
@Schema(name="Account对象", description="用户基本信息表")
public class Account implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @SchemaProperty(name = "主键ID")
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    @SchemaProperty(name = "加密后的密码")
    private String password;

    @SchemaProperty(name = "邮箱")
    private String email;

    @SchemaProperty(name = "手机号")
    private String phone;

    @SchemaProperty(name = "帐号状态（1:正常, 0:停用, 2:锁定）")
    private Integer status;

    @SchemaProperty(name = "删除标志")
    private Long deleted;

    @SchemaProperty(name = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @SchemaProperty(name = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
