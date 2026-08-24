package com.gp_01.file.service.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gp_01.common.context.UserContext;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.common.utils.TimeUtils;
import com.gp_01.file.model.domain.dto.*;
import com.gp_01.file.model.domain.po.UserFile;
import com.gp_01.file.model.domain.query.PageFilesQuery;
import com.gp_01.file.model.domain.vo.*;
import com.gp_01.file.service.config.FileServiceProperties;
import com.gp_01.file.service.mapper.FileObjectMapper;
import com.gp_01.file.service.mapper.UserFileMapper;
import com.gp_01.file.service.service.IUserFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gp_01.file.service.util.FileUtils;
import com.gp_01.user.api.client.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

    private final UserFileMapper userFileMapper;

    private final FileUtils fileUtils;

    private final FileObjectMapper fileObjectMapper;

    private final FileServiceProperties fileServiceProperties;

    private final UserClient userClient;


    /**
     * 创建单层文件夹
     */
    @Override
    public Long makeDir(MakeDirDTO dto) {
        Long userId = UserContext.getUser();
        //判断是否存在
        UserFile exist = fileExist(userId, dto.getParentId(), dto.getFileName());
        if (exist != null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "文件夹" + dto.getFileName() + "已存在");
        }
        //组装数据
        UserFile userFile = new UserFile();
        userFile.setUserId(userId);
        userFile.setParentId(dto.getParentId());
        userFile.setFileName(dto.getFileName());
        userFile.setIsDirectory(1);

        //保存到数据库
        super.save(userFile);

        return userFile.getId();
    }

    /**
     * 支持创建层级文件夹，如果当前目录存在同名文件夹，将继续创建
     */
    @Override
    public Long makeMultiDir(MakeMultiDirDTO dto) {
        String[] dirNames = dto.getRelativePath().split("/");
        Long userId = UserContext.getUser();
        UserFile exist = new UserFile();
        Long parentId = dto.getParentId();
        for (String dirName : dirNames) {
            //判断是否存在
            if (exist != null) {
                exist = fileExist(userId, parentId, dirName);
            }
            if (exist != null) {
                parentId = exist.getId();
                continue;
            }
            //组装数据
            UserFile userFile = new UserFile();
            userFile.setUserId(userId);
            userFile.setParentId(parentId);
            userFile.setFileName(dirName);
            userFile.setIsDirectory(1);

            //保存到数据库
            super.save(userFile);
            parentId = userFile.getId();
        }
        return parentId;
    }

    @Override
    public void reName(Long id, String fileName) {
        Long userId = UserContext.getUser();
        //查数据
        UserFile one = lambdaQuery().eq(UserFile::getId, id).eq(UserFile::getUserId, userId).one();
        if (one == null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "该数据不存在");
        }
        //判断是否有同名文件
        boolean exist = existSameFileName(userId, one.getParentId(), fileName);
        if (exist) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "该目录存在同名文件");
        }
        //修改
        lambdaUpdate()
                .eq(UserFile::getId, id)
                .set(UserFile::getFileName, fileName)
                .set(UserFile::getUpdateTime, LocalDateTime.now())
                .update();
    }

    private boolean existSameFileName(Long userid, Long parentId, String fileName) {
        UserFile one = lambdaQuery()
                .eq(UserFile::getUserId, userid)
                .eq(UserFile::getParentId, parentId)
                .eq(UserFile::getFileName, fileName)
                .eq(UserFile::getDeleted, 0L)
                .one();
        return one != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        Long userId = UserContext.getUser();
        //查数据
        UserFile one = lambdaQuery().eq(UserFile::getId, id).eq(UserFile::getUserId, userId).one();
        if (one == null) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "该数据不存在");
        }
        Long timeStamp = Instant.now().toEpochMilli();

        //逻辑删除
        super.lambdaUpdate()
                .eq(UserFile::getId, id)
                .eq(UserFile::getUserId, userId)
                .set(UserFile::getDeleted, timeStamp)
                .update();

        //减少已使用空间大小
        userClient.minusUsedStoreSize(one.getFileSize());


    }

    @Override
    public PageResult<UserFile> listFileByParentId(PageFilesQuery query) {
        Long userId = UserContext.getUser();
        //条件分页查询
        LambdaQueryChainWrapper<UserFile> userFileWrapper = lambdaQuery()
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getDeleted, 0)
                .eq(UserFile::getParentId, query.getParentId())
                .ne(UserFile::getId,0)
                .orderByDesc(UserFile::getIsDirectory);
        if (query.getSortBy() != null) {
            userFileWrapper
                    .orderBy(query.getSortBy().equals("fileSize"), query.getIsAsc(), UserFile::getFileSize)
                    .orderBy(query.getSortBy().equals("fileName"), query.getIsAsc(), UserFile::getFileName)
                    .orderBy(query.getSortBy().equals("updateTime"), query.getIsAsc(), UserFile::getUpdateTime)
                    .orderBy(query.getSortBy().equals("fileType"), query.getIsAsc(), UserFile::getMediaCategory);
        }
        Page<UserFile> page = userFileWrapper.page(query.toPage());
        List<UserFile> records = page.getRecords();

        //如果没数据返回空集合
        if (records == null || records.isEmpty()) {
            return PageResult.empty(page);
        }

        return PageResult.of(page);
    }

    //TODO 未排序
    @Override
    public PageResult<ListRecycleBinVO> recyclePage(PageParams params) {
        Long userId = UserContext.getUser();
        long minusDay = fileServiceProperties.getRecycleSaveDay() * 60 * 60 * 24;
        long limitTime = Instant.now().minusSeconds(minusDay).toEpochMilli();
        //查数据库
        Page<UserFile> page = lambdaQuery()
                .eq(UserFile::getUserId, userId)
                .ge(UserFile::getDeleted, limitTime)
                .page(params.toPage());
        if (page.getRecords() == null) {
            return PageResult.empty();
        }
        List<ListRecycleBinVO> res = new ArrayList<>();
        for (UserFile userFile : page.getRecords()) {
            ListRecycleBinVO vo = new ListRecycleBinVO();
            BeanUtils.copyProperties(userFile, vo);
            //设置独有属性
            LocalDateTime deleteTime = TimeUtils.milliToLocalDateTime(userFile.getDeleted());
            long validDay = fileServiceProperties.getRecycleSaveDay() - ChronoUnit.DAYS.between(deleteTime, LocalDateTime.now());
            vo.setValidDay(validDay);
            vo.setDeleteTime(deleteTime);
            res.add(vo);
        }
        return new PageResult<>(page.getTotal(), page.getSize(), page.getCurrent(), res);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreFile(List<Long> ids) {
        Long userId = UserContext.getUser();
        //查数据库获取所有将要恢复的文件信息
        List<UserFile> userFileList = lambdaQuery()
                .eq(UserFile::getUserId, userId)
                .in(UserFile::getId, ids)
                .list();
        if (userFileList.isEmpty()) {
            return;
        }
        //同一个目录下的文件进行分组
        Map<Long, List<UserFile>> userFileGroupMap = userFileList
                .stream()
                .collect(Collectors.groupingBy(UserFile::getParentId));

        for (Map.Entry<Long, List<UserFile>> entry : userFileGroupMap.entrySet()) {
            Long parentId = entry.getKey();
            List<UserFile> userFiles = entry.getValue();
            //按照每个目录进行查数据库获取目录下未删除文件名
            Set<String> fileNames = lambdaQuery()
                    .eq(UserFile::getUserId, userId)
                    .eq(UserFile::getParentId, parentId)
                    .eq(UserFile::getDeleted, 0L)
                    .list()
                    .stream()
                    .map(UserFile::getFileName)
                    .collect(Collectors.toSet());
            //给当下每个文件进行重新创建安全文件名
            for (UserFile userFile : userFiles) {
                String safeFileName = fileUtils.getSafeFileName(userFile.getFileName(), fileNames);
                fileNames.add(safeFileName);
                userFile.setFileName(safeFileName);
                userFile.setDeleted(0L);

            }
        }
        //统一修改数据库
        updateBatchById(userFileList);
    }


    @Override
    public PageResult<UserFile> listFileByTypeAndPage(PageParams params, Integer type) {
        //获取登录用户
        Long userId = UserContext.getUser();
        //条件查询
        Page<UserFile> page = lambdaQuery()
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getMediaCategory, type)
                .eq(UserFile::getDeleted, 0)
                .page(params.toPage());

        List<UserFile> records = page.getRecords();
        if (records.isEmpty()) {
            return PageResult.empty(page);
        }
        return PageResult.of(page);
    }

    @Override
    public void moveFile(Long fileId, Long targetId) {
        Long userId = UserContext.getUser();
        UserFile one = super.lambdaQuery()
                .eq(UserFile::getId, fileId)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getDeleted, 0)
                .one();
        if (one == null) {
            throw new BadRequestException(ErrorCode.RECOURSE_NOT_FOUND_ERROR);
        }
        //判断目标目录上有没有同名文件
        Integer cnt = userFileMapper.existsSameFileName(fileId, userId, targetId);
        if (cnt != 0) {
            throw new BadRequestException(ErrorCode.BUSINESS_ERROR.getCode(), "目标目录存在同名文件");
        }
        //更改该文件的文件夹id
        one.setParentId(targetId);
        super.updateById(one);
    }

    @Override
    public List<UserFile> listDirByParentId(Long parentId) {
        Long userId = UserContext.getUser();
        List<UserFile> list = super.lambdaQuery()
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getParentId, parentId)
                .eq(UserFile::getDeleted, 0)
                .eq(UserFile::getIsDirectory, 1)
                .ne(UserFile::getId,0)
                .list();
        if (list == null) {
            return List.of();
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecycleFileBatch(List<Long> ids) {
        Long userId = UserContext.getUser();
        //查询要被删除的fileId
        List<UserFile> list = super.lambdaQuery()
                .eq(UserFile::getUserId, userId)
                .in(UserFile::getId, ids)
                .ne(UserFile::getDeleted, 0)
                .list();

        List<Long> dirIds = new ArrayList<>();
        List<Long> fileIds = new ArrayList<>();
        for (UserFile userFile : list) {
            if (userFile.getIsDirectory() == 1) {
                dirIds.add(userFile.getId());
            } else {
                fileIds.add(userFile.getObjectId());
            }
        }
        //查询出文件夹的所有文件
        List<UserFile> files = null;
        if (!dirIds.isEmpty()) {
            files = userFileMapper.listFilesByDirIds(dirIds);
            for (UserFile file : files) {
                if (file.getIsDirectory() == 0) {
                    fileIds.add(file.getObjectId());
                }
            }
        }
        if(!fileIds.isEmpty()){
            //将该文件引用数-1
            fileObjectMapper.minusRefCountBatch(fileIds);
        }

        //删除文件
        HashSet<Long> deleteIds = new HashSet<>();
        if (files != null) {
            deleteIds.addAll(files.stream().map(UserFile::getId).collect(Collectors.toSet()));
        }
        deleteIds.addAll(list.stream().map(UserFile::getId).collect(Collectors.toSet()));
        super.removeBatchByIds(deleteIds);
    }


    /**
     * 判断文件或文件夹是否存在
     */
    private UserFile fileExist(Long userId, Long parentId, String fileName) {
        return lambdaQuery()
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getParentId, parentId)
                .eq(UserFile::getFileName, fileName)
                .eq(UserFile::getDeleted, 0)
                .one();
    }
}
