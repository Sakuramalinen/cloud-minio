package com.gp_01.file.service.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gp_01.file.service.config.MinioConfig;
import com.gp_01.file.service.util.MinioUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@RequiredArgsConstructor
public class minioTask {


    private final MinioUtils minioUtils;

    private final MinioConfig minioConfig;

}
