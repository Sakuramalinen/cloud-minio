package com.gp_01.model.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.gp_01.model.enums.UserStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 用户基本信息表
 * </p>
 *
 * @author shenyongqi
 * @since 2026-06-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
@Schema(name = "User对象", description = "用户基本信息表")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @SchemaProperty(name = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @SchemaProperty(name = "邮箱")
    private String email;

    @SchemaProperty(name = "手机号")
    private String phone;

    @SchemaProperty(name = "加密后的密码")
    private String password;

    @SchemaProperty(name = "用户昵称")
    private String nickname;

    @SchemaProperty(name = "头像URL地址")
    private String avatar;

    @SchemaProperty(name = "帐号状态")
    private UserStatusEnum status;

    @SchemaProperty(name = "删除标志")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @SchemaProperty(name = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @SchemaProperty(name = "更新时间")
    private LocalDateTime updateTime;

}
