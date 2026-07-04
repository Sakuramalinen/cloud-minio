package com.gp_01.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gp_01.model.domain.dto.RegisterDTO;
import com.gp_01.model.domain.po.User;

/**
 * <p>
 * 用户基本信息表 服务类
 * </p>
 *
 * @author shenyongqi
 * @since 2026-06-18
 */
public interface IUserService extends IService<User> {

    void register(RegisterDTO registerDTO);

    User getUserByPhone(String phone);

    User getUserByEmail(String email);
}
