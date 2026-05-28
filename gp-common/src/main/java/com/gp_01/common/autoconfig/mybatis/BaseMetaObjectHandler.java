package com.gp_01.common.autoconfig.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.gp_01.common.constants.DataBaseConstants;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.gp_01.common.constants.DataBaseConstants.UPDATE_TIME;

@Component
public class BaseMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        //创建时间
        setCreateTime(metaObject);
        //修改时间
        setUpdateTime(metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        metaObject.setValue(UPDATE_TIME, null);
        setUpdateTime(metaObject);
    }


    private void setCreateTime(MetaObject metaObject){
        this.strictInsertFill(metaObject, DataBaseConstants.CREATE_TIME, LocalDateTime::now, LocalDateTime.class);
    }
    private void setUpdateTime(MetaObject metaObject){
        this.strictUpdateFill(metaObject, UPDATE_TIME, LocalDateTime::now, LocalDateTime.class);
    }
}
