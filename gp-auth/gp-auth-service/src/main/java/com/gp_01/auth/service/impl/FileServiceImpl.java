package com.gp_01.auth.service.impl;

import com.gp_01.auth.config.JWTProperties;
import com.gp_01.auth.service.IFileService;
import com.gp_01.auth.utils.JWTUtils;
import com.gp_01.common.context.UserContext;
import com.gp_01.common.domain.Result;
import com.gp_01.api.client.UserFileClient;
import com.gp_01.file.model.domain.vo.DownloadPrivilegeVO;
import com.gp_01.file.model.domain.vo.FileDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static com.gp_01.common.constants.HttpHeaderConstants.FILE_DOWNLOAD_PATH_HEADER;
import static com.gp_01.common.constants.HttpHeaderConstants.FILE_DOWNLOAD_USERID_HEADER;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements IFileService {

    private final UserFileClient userFileClient;

    private final JWTUtils jwtUtils;

    private final JWTProperties jwtProperties;


    @Override
    public String getDownloadPrivilege(Long id) {

        Result<String> result = userFileClient.getDownloadPath(id);
        String path = result.getData();
        Long userId = UserContext.getUser();
        HashMap<String, Object> claims = new HashMap<>();
        claims.put(FILE_DOWNLOAD_PATH_HEADER, path);
        claims.put(FILE_DOWNLOAD_USERID_HEADER, userId);

        return jwtUtils.createToken(claims, jwtProperties.getExpire());


    }

    @Override
    public String getUploadPrivilege(Long id) {

        return "";
    }
}
