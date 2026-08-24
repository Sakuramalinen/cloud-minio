package com.gp_01.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gp_01.user.model.domain.po.User;
import jakarta.validation.constraints.NotNull;

/**
 * <p>
 * 用户基本信息表 服务类
 * </p>
 *
 * @author shenyongqi
 * @since 2026-06-18
 */
public interface IUserService extends IService<User> {


    User getUserInfo(@NotNull Long accountId);

    void updateUserInfo(User user);

    User createUser();

    void incrementUsedStoreSize(Long size);

    void minusUsedStoreSize(Long size);
}
