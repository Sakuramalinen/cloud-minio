package com.gp_01.file.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gp_01.common.context.UserContext;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.common.exception.CommonException;
import com.gp_01.file.model.domain.cache.redis.UploadAvatarCache;
import com.gp_01.file.model.domain.po.UserAvatar;
import com.gp_01.file.model.domain.vo.ListHistoryAvatarVO;
import com.gp_01.file.service.constants.RedisKeyFormatter;
import com.gp_01.file.service.mapper.UserAvatarMapper;
import com.gp_01.file.service.oss.FileManipulator;
import com.gp_01.file.service.oss.OSS;
import com.gp_01.file.service.service.IUserAvatarService;
import com.gp_01.file.service.util.FileUtils;
//import com.gp_01.file.service.util.MinioUtils;
import com.gp_01.file.service.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shenyongqi
 * @since 2026-08-24
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserAvatarServiceImpl extends ServiceImpl<UserAvatarMapper, UserAvatar> implements IUserAvatarService {

    private final FileUtils fileUtils;

    private final OSS oss;

    private final FileManipulator fileManipulator;

    private final RedisUtils redisUtils;


    @Override
    public String uploadAvatar(String filename, String contentType, Long fileSize) {

        Long userId = UserContext.getUser();
        String uuid = UUID.randomUUID().toString();
        String extendName = fileUtils.getFileExtendName(filename);
        String alias = uuid + extendName;
        //申请预签名url
        String avatarFileStorePath = fileUtils.getAvatarFileStorePath(alias, userId);
        String url = fileManipulator.generateUploadPreSignedUrl(oss.getDefaultBucket(), avatarFileStorePath, contentType, fileSize, Duration.ofHours(1));
        //将地址存到内存
        String key = RedisKeyFormatter.UploadAvatarInfoKey(userId);
        UploadAvatarCache cache = new UploadAvatarCache(avatarFileStorePath, filename, contentType, fileSize);
        redisUtils.setObject(key, cache, 5L, TimeUnit.MINUTES);
        return url;
    }

    @Override
    public String previewAvatar(Long id) {
        Long userId = UserContext.getUser();
        UserAvatar one = super.lambdaQuery()
                .eq(UserAvatar::getId, id)
                .eq(UserAvatar::getUserId, userId)
                .one();
        if (one == null) {
            log.error("user与user_avatar数据库数据不一致 user -> userId: {}, user_avatar表 -> id: {}", userId, id);
            throw new CommonException(ErrorCode.SERVICE_ERROR);
        }

        return fileManipulator.generatePreviewPreSignedUrl(oss.getDefaultBucket(), one.getObjectPath(), one.getContentType(), Duration.ofHours(1));
    }

    @Override
    public List<ListHistoryAvatarVO> previewHistoryAvatarList() {
        Long userId = UserContext.getUser();
        List<UserAvatar> list = super.lambdaQuery()
                .eq(UserAvatar::getUserId, userId)
                .orderByDesc(UserAvatar::getCreateTime)
                .list();

        List<ListHistoryAvatarVO> res = new ArrayList<>();
        for (UserAvatar userAvatar : list) {
            //获取预签名url
            String url = fileManipulator.generatePreviewPreSignedUrl(oss.getDefaultBucket(), userAvatar.getObjectPath(), userAvatar.getContentType(), Duration.ofHours(1));
            ListHistoryAvatarVO vo = new ListHistoryAvatarVO()
                    .setId(userAvatar.getId())
                    .setUrl(url)
                    .setSize(userAvatar.getFileSize());
            res.add(vo);

        }
        return res;
    }

    @Override
    public Long persistenceAvatar() {
        Long userId = UserContext.getUser();
        String key = RedisKeyFormatter.UploadAvatarInfoKey(userId);
        UploadAvatarCache uploadAvatarCache = redisUtils.getObject(key, UploadAvatarCache.class);

        if (uploadAvatarCache.getObjectPath() == null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "上传超时，请重新上传");
        }

        UserAvatar userAvatar = new UserAvatar()
                .setUserId(userId)
                .setContentType(uploadAvatarCache.getContentType())
                .setFileSize(uploadAvatarCache.getFileSize())
                .setObjectPath(uploadAvatarCache.getObjectPath());

        //存数据库
        super.save(userAvatar);
        return userAvatar.getId();
    }
}
