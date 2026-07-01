package com.gp_01.auth.service.impl;

import com.gp_01.auth.service.IFileService;
import com.gp_01.auth.utils.JWTUtils;
import com.gp_01.common.domain.Result;
import com.gp_01.api.client.UserFileClient;
import com.gp_01.file.model.domain.vo.FileDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements IFileService {

    private final UserFileClient userFileClient;

    private final JWTUtils jwtUtils;


    @Override
    public String getDownloadPrivilege(Long id) {

        Result<FileDetail> result = userFileClient.getFileDetail(id);
        FileDetail fileDetail = result.getData();
        String createTime = fileDetail.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String downloadPath = "original/" + createTime + "/" + fileDetail.getFileMd5() + fileDetail.getFileSuffix();
        return jwtUtils.createFileToken(downloadPath);


    }
}
