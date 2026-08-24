package com.gp_01.user.model.domain.po;

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
 * 用户信息表
 * </p>
 *
 * @author shenyongqi
 * @since 2026-08-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
@Schema(name="User对象", description="用户信息表")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @SchemaProperty(name = "账户id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;


    @SchemaProperty(name = "用户昵称")
    private String nickname;

    @SchemaProperty(name = "当前使用头像id, 0表示没有设置头像")
    private Long avatarId;

    @SchemaProperty(name = "vip过期时间")
    private LocalDateTime vipExpireTime;

    @SchemaProperty(name = "存储空间")
    private Long totalStoreSize;

    @SchemaProperty(name = "已使用存储空间")
    private Long usedStoreSize;

    @SchemaProperty(name = "修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;




}
