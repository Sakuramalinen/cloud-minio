package com.gp_01.file.service.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gp_01.file.model.domain.po.FileSlice;
import com.gp_01.file.service.mapper.FileSliceMapper;
import com.gp_01.file.service.service.IFileSliceService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文件分片上传临时表 服务实现类
 * </p>
 *
 * @author shenyongqi
 * @since 2026-07-19
 */
@Service
public class FileSliceServiceImpl extends ServiceImpl<FileSliceMapper, FileSlice> implements IFileSliceService {

}
