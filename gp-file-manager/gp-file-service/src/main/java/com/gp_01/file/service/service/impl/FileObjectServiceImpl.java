package com.gp_01.file.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gp_01.file.model.domain.po.FileObject;
import com.gp_01.file.service.mapper.FileObjectMapper;
import com.gp_01.file.service.service.IFileObjectService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * MinIO物理文件实体表 服务实现类
 * </p>
 *
 * @author shenyongqi
 * @since 2026-07-19
 */
@Service
public class FileObjectServiceImpl extends ServiceImpl<FileObjectMapper, FileObject> implements IFileObjectService {

}
