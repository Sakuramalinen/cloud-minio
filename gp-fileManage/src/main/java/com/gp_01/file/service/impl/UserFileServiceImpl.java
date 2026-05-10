package com.gp_01.file.service.impl;

import com.gp_01.common.exception.BadRequestException;
import com.gp_01.common.exception.ForbiddenException;
import com.gp_01.common.utils.FileTypeResolver;
import com.gp_01.file.domain.po.FileBase;
import com.gp_01.file.domain.po.UserFile;
import com.gp_01.file.mapper.UserFileMapper;
import com.gp_01.file.service.IFileBaseService;
import com.gp_01.file.service.IUserFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.gp_01.common.enums.FileTypeEnum.DIRECTORY;

/**
 * <p>
 * 用户逻辑文件表 服务实现类
 * </p>
 *
 * @author employee_01
 * @since 2026-05-08
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserFileServiceImpl extends ServiceImpl<UserFileMapper, UserFile> implements IUserFileService {

    private final IFileBaseService fileBaseService;

    private final UserFileMapper userFileMapper;

    @Override
    public void uploadFile(MultipartFile file, Long parentId, String md5Hex) {
        //TODO 获取用户id
        Long userId = 101L;
        boolean exist = fileExist(userId, parentId, file.getName());
        if (exist) {
            throw new BadRequestException("文件已存在");
        }
        String fileName = file.getOriginalFilename();
        if (StringUtils.isEmpty(fileName)) {
            throw new BadRequestException("文件名异常");
        }
        //准备数据
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        FileBase fileBase = fileBaseService.uploadFile(file, md5Hex);

        //组装数据
        UserFile userFile = new UserFile();
        userFile.setUserId(userId);
        userFile.setFileId(fileBase.getId());
        userFile.setParentId(parentId);
        userFile.setFileName(fileName);
        userFile.setFileSuffix(suffix);
        userFile.setFileSize(file.getSize());
        userFile.setFileType(FileTypeResolver.parse(file.getContentType()));
        super.save(userFile);
    }

    @Override
    public void makeDir(Long parentId, String fileName) {
        //TODO 获取用户id
        Long userId = 101L;
        //判断是否存在
        boolean exist = fileExist(userId, parentId, fileName);
        if (exist) {
            throw new BadRequestException("该文件夹已存在");
        }
        //组装数据
        UserFile userFile = new UserFile();
        userFile.setUserId(userId);
        userFile.setParentId(parentId);
        userFile.setFileName(fileName);
        userFile.setFileType(DIRECTORY);

        //保存到数据库
        super.save(userFile);
    }

    @Override
    public void reName(Long id, String fileName) {
        //TODO 获取当前用户id
        Long userId = 101L;
        //查数据
        UserFile one = lambdaQuery().eq(UserFile::getId, id).one();
        if (one == null) {
            throw new BadRequestException("空数据");
        }
        if (!Objects.equals(one.getUserId(), userId)) {
            throw new ForbiddenException("用户无权限操作");
        }
        //修改
        lambdaUpdate().eq(UserFile::getId, id).set(UserFile::getFileName, fileName).update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        //TODO 获取当前用户id
        Long userId = 101L;
        //查数据
        UserFile one = lambdaQuery().eq(UserFile::getId, id).one();
        if (one == null) {
            throw new BadRequestException("空数据");
        }
        if (!Objects.equals(one.getUserId(), userId)) {
            throw new ForbiddenException("用户无权限操作");
        }
        Long timeStamp = Instant.now().toEpochMilli();
        //需要修改的文件id集合
        List<Long> ids;
        if (Objects.equals(one.getFileType(), DIRECTORY)) {
            ids = userFileMapper.listFileIdByParentId(id, userId);
        } else {
            ids = List.of(one.getFileId());
        }
        //逻辑删除
        userFileMapper.deleteFile(id, userId, timeStamp);

        //TODO 可以异步 物理文件表的该文件引用-1
        if(!ids.isEmpty()){
            fileBaseService.subtractRefCount(ids);
        }
    }

    @Override
    public List<UserFile> listFileByParentId(Long parentId) {
        //TODO 获取当前登录用户
        Long userId = 101L;
        parentId = parentId == null ? 0 : parentId;
        //条件查询
        List<UserFile> UserFileList = lambdaQuery()
                .eq(UserFile::getParentId, parentId)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getDeleted, 0)
                .list();
        //如果没数据返回空集合
        if (UserFileList == null || UserFileList.isEmpty()) {
            return List.of();
        }

        return UserFileList;
    }

    /**
     * 判断文件或文件夹是否存在
     *
     * @param userId
     * @param parentId
     * @param fileName
     * @return
     */
    private boolean fileExist(Long userId, Long parentId, String fileName) {
        UserFile one = lambdaQuery()
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getParentId, parentId)
                .eq(UserFile::getFileName, fileName)
                .eq(UserFile::getDeleted, 0)
                .one();
        return one != null;
    }
}
