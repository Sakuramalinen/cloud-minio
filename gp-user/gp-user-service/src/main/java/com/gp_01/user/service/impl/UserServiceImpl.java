package com.gp_01.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;

import com.gp_01.model.domain.dto.RegisterDTO;
import com.gp_01.model.domain.po.User;
import com.gp_01.model.enums.UserStatusEnum;
import com.gp_01.user.mapper.UserMapper;
import com.gp_01.user.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * <p>
 * 用户基本信息表 服务实现类
 * </p>
 *
 * @author shenyongqi
 * @since 2026-06-18
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private final PasswordEncoder encoder;
    @Override
    public void register(RegisterDTO dto) {
        //TODO 校验验证码
        //判断是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getEmail() != null, User::getEmail, dto.getEmail())
                .eq(dto.getPhone() != null, User::getPhone, dto.getPhone());
        User one = super.getOne(wrapper);
        if (one != null) {
            log.debug("用户已存在");
            throw new BadRequestException(ErrorCode.USER_EXIST_ERROR);
        }
        //密码加密
        String encodePassword = encoder.encode(dto.getPassword());
        User user = new User();
        user.setPassword(encodePassword);
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());

        //设置默认参数
        setDefault(user);
        //存数据库
        super.save(user);
    }

    @Override
    public User getUserByPhone(String phone) {
        return lambdaQuery().eq(User::getPhone, phone).one();
    }

    @Override
    public User getUserByEmail(String email) {
        return lambdaQuery().eq(User::getEmail, email).one();
    }

    public void setDefault(User user){
        //TODO设置默认昵称
        UUID uuid = UUID.randomUUID();
        user.setNickname(uuid.toString());
        //TODO设置默认头像
        String url = "https://gips3.baidu.com/it/u=4028841722,3177937756&fm=3074&app=3074&f=PNG?w=2048&h=2048";
        user.setAvatar(url);
        user.setStatus(UserStatusEnum.NORMAL);
        user.setDeleted(0);
    }
}
