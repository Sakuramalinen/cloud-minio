package com.gp_01.user.service.impl;

import com.gp_01.auth.model.po.Account;
import com.gp_01.common.context.UserContext;
import com.gp_01.common.domain.Result;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.file.api.client.UserFileClient;
import com.gp_01.file.model.domain.dto.userFile.CreateRootDTO;
import com.gp_01.user.config.UserConfig;
import com.gp_01.user.model.domain.po.User;
import com.gp_01.user.mapper.UserMapper;
import com.gp_01.user.model.domain.vo.UserInfoVo;
import com.gp_01.user.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shenyongqi.auth.api.client.AccountClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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


    private final UserFileClient userFileClient;

    private final UserConfig userConfig;

    private final AccountClient accountClient;

    @Override
    public UserInfoVo getUserInfo(Long accountId) {
        Account account = accountClient.getAccount(accountId);
        if (account == null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR);
        }
        User user = super.getById(accountId);
        UserInfoVo vo = new UserInfoVo();
        BeanUtils.copyProperties(user, vo);
        vo.setEmail(account.getEmail())
                .setStatus(account.getStatus())
                .setPhoneNumber(account.getPhone());
        return vo;
    }

    @Override
    public void updateUserInfo(User user) {
        Long userId = UserContext.getUser();
        if(!user.getId().equals(userId)){
            throw new BadRequestException(ErrorCode.AUTHORITY_ERROR.getCode(),"暂无权限");
        }
        super.lambdaUpdate()
                .eq(User::getId, user.getId())
                .set(!user.getNickname().isBlank(), User::getNickname, user.getNickname())
                .set(user.getAvatarId() != null, User::getAvatarId, user.getAvatarId())
                .update();

    }

    @Override
    public User createUser() {

        User user = new User();
        setUserDefault(user);

        super.save(user);
        //创建根目录
        Result<Long> root = userFileClient.createRoot(new CreateRootDTO(user.getId()));
        user.setRootId(root.getData());
        updateById(user);
        return user;
    }

    @Override
    public void incrementUsedStoreSize(Long size,Long userId) {
        if (size == null){
            throw new BadRequestException(ErrorCode.PARAM_ERROR);
        }
        super.lambdaUpdate()
                .setSql("used_store_size = greatest(used_store_size + {0}, 0)", size)
                .eq(User::getId, userId)
                .update();
    }



    private void setUserDefault(User user){
        //TODO 默认昵称
        String nickname = UUID.randomUUID().toString();
        LocalDateTime vipExpireTime = LocalDateTime.of(1900, 1,1,0,0);

        user.setNickname(nickname);
        //默认空间大小
        user.setTotalStoreSize(userConfig.getDefaultUserStore());
        user.setUsedStoreSize(0L);
        user.setVipExpireTime(vipExpireTime);
        user.setAvatarId(0L);

    }
}
