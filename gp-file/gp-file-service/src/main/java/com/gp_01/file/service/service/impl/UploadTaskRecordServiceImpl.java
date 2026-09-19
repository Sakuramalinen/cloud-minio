package com.gp_01.file.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gp_01.common.context.UserContext;

import com.gp_01.common.domain.Result;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.file.model.domain.dto.UploadProgressSaveDTO;
import com.gp_01.file.model.domain.dto.taskRecord.CreateUploadTaskRecordDTO;
import com.gp_01.file.model.domain.po.UploadTaskRecord;
import com.gp_01.file.model.domain.po.UserFile;
import com.gp_01.file.service.config.FileServiceProperties;
import com.gp_01.file.service.constants.RabbitmqFileConstants;
import com.gp_01.file.service.constants.RedisKeyFormatter;
import com.gp_01.file.service.mapper.UploadTaskRecordMapper;
import com.gp_01.file.service.mapper.UserFileMapper;
import com.gp_01.file.service.oss.OSS;
import com.gp_01.file.service.service.IUploadTaskRecordService;
import com.gp_01.file.service.util.FileUtils;
import com.gp_01.file.service.util.RedisUtils;
import com.gp_01.user.api.client.UserClient;
import com.gp_01.user.model.domain.po.User;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 未完成的上传任务映射表 服务实现类
 * </p>
 *
 * @author shenyongqi
 * @since 2026-07-23
 */
@Service
@RequiredArgsConstructor
public class UploadTaskRecordServiceImpl extends ServiceImpl<UploadTaskRecordMapper, UploadTaskRecord> implements IUploadTaskRecordService {




}
