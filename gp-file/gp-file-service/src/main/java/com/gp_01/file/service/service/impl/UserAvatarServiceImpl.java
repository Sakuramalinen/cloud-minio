package com.gp_01.file.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gp_01.common.context.UserContext;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.common.exception.CommonException;
import com.gp_01.file.model.domain.po.UserAvatar;
import com.gp_01.file.model.domain.vo.ListHistoryAvatarVO;
import com.gp_01.file.service.config.MinioConfig;
import com.gp_01.file.service.constants.RedisKeyFormatter;
import com.gp_01.file.service.mapper.UserAvatarMapper;
import com.gp_01.file.service.oss.OSS;
import com.gp_01.file.service.oss.preview.Previewer;
import com.gp_01.file.service.oss.preview.product.MinioPreviewer;
import com.gp_01.file.service.oss.upload.Uploader;
import com.gp_01.file.service.oss.upload.product.MinioUploader;
import com.gp_01.file.service.service.IUserAvatarService;
import com.gp_01.file.service.util.FileUtils;
import com.gp_01.file.service.util.MinioUtils;
import com.gp_01.file.service.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    private final Uploader uploader;

    private final Previewer previewer;

    private final OSS oss;

    private final RedisUtils redisUtils;

    private final MinioUtils minioUtils;


    @Override
    public String uploadAvatar(String filename) {

        Long userId = UserContext.getUser();
        String uuid = UUID.randomUUID().toString();
        String extendName = fileUtils.getFileExtendName(filename);
        String alias = uuid + extendName;
        //申请预签名url
        String avatarFileStorePath = fileUtils.getAvatarFileStorePath(alias, userId);
        String url = uploader.uploadPreSign(oss.getBucketName(), avatarFileStorePath, 5, TimeUnit.MINUTES);
        //将地址存到内存
        String key = RedisKeyFormatter.UploadAvatarInfoKey(userId);
        redisUtils.set(key, avatarFileStorePath, 5L, TimeUnit.MINUTES);
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

        return previewer.previewPreSignUrl(oss.getBucketName(), one.getObjectPath(), one.getContentType(), 1, TimeUnit.DAYS);
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
            String url = previewer.previewPreSignUrl(oss.getBucketName(), userAvatar.getObjectPath(), userAvatar.getContentType(), 1, TimeUnit.DAYS);
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
        String objectPath = redisUtils.get(key);
        if (objectPath == null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "上传超时，请重新上传");
        }

        //获取真实content-type
        String contentType;
        Long size;
        //获取文件大小
        try {
            contentType = fileUtils.getContentTypeByFileBinary(objectPath);
            size = minioUtils.getFileStatus(oss.getBucketName(), objectPath).getSize();
        } catch (Exception e) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "上传失败，请重新上传");
        }

        UserAvatar userAvatar = new UserAvatar()
                .setUserId(userId)
                .setContentType(contentType)
                .setFileSize(size)
                .setObjectPath(objectPath);

        //存数据库
        super.save(userAvatar);
        return userAvatar.getId();
    }
}
