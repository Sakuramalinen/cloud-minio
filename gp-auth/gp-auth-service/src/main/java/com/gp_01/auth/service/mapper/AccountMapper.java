package com.gp_01.auth.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gp_01.auth.model.po.Account;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
