package com.gp_01.file.service.service.impl;

import com.gp_01.file.service.config.MinioConfig;
import com.gp_01.file.model.domain.po.FileBase;
import com.gp_01.file.service.mapper.FileBaseMapper;
import com.gp_01.file.service.service.IFileBaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gp_01.file.service.util.ThumbnailUtils;
import com.gp_01.file.service.util.MinioUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * <p>
 * 文件信息表 服务实现类
 * </p>
 *
 * @author employee_01
 * @since 2026-05-07
 */
@Service
@RequiredArgsConstructor
public class FileBaseServiceImpl extends ServiceImpl<FileBaseMapper, FileBase> implements IFileBaseService {







}
