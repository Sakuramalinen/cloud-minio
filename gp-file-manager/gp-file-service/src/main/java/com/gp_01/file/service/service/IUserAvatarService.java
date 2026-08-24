package com.gp_01.file.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gp_01.file.model.domain.po.UserAvatar;
import com.gp_01.file.model.domain.vo.ListHistoryAvatarVO;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author shenyongqi
 * @since 2026-08-24
 */
public interface IUserAvatarService extends IService<UserAvatar> {

    String uploadAvatar(@NotNull String filename);

    String previewAvatar(Long id);


    List<ListHistoryAvatarVO> previewHistoryAvatarList();

    Long persistenceAvatar();

}
