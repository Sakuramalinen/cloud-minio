package com.gp_01.file.domain.po;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.gp_01.common.enums.FileTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户逻辑文件表
 * </p>
 *
 * @author employee_01
 * @since 2026-05-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_file")
@Schema(name="UserFile对象", description="用户逻辑文件表")
public class UserFile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @SchemaProperty(name = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @SchemaProperty(name = "用户id")
    private Long userId;

    @SchemaProperty(name = "文件id")
    private Long fileId;

    @SchemaProperty(name = "父文件夹ID(根目录规定为0)")
    private Long parentId;

    @SchemaProperty(name = "文件或文件夹名称")
    private String fileName;

    @SchemaProperty(name = "文件后缀名")
    private String fileSuffix;

    @SchemaProperty(name = "文件大小字节")
    private Long fileSize;

    @SchemaProperty(name = "MIME类型")
    private String contentType;

    @SchemaProperty(name = "文件类型")
    private FileTypeEnum fileType;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @SchemaProperty(name = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @SchemaProperty(name = "修改时间")
    private LocalDateTime updateTime;

    @SchemaProperty(name = "逻辑删除 0存在 时间戳删除")
    private Long deleted;
    //TODO 文件路径 userid/主键id/主键id
    // String path

    public static SFunction<UserFile, ?> getSortByColumn(String sortBy){
        if(StringUtils.isEmpty(sortBy))return UserFile::getCreateTime;
        return switch(sortBy){
            case "updateTime" -> UserFile::getUpdateTime;
            case "fileName" -> UserFile::getFileName;
            case "fileSize" -> UserFile::getFileSize;
            default  -> UserFile::getCreateTime;
        };
    }
}
