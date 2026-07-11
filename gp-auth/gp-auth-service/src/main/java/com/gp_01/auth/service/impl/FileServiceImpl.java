package com.gp_01.auth.service.impl;

import com.gp_01.auth.config.JWTProperties;
import com.gp_01.auth.service.IFileService;
import com.gp_01.auth.utils.JWTUtils;
import com.gp_01.common.context.UserContext;
import com.gp_01.common.domain.Result;
import com.gp_01.api.client.UserFileClient;
import com.gp_01.file.model.domain.vo.DownloadInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;

import static com.gp_01.common.constants.HttpHeaderConstants.*;


@Service
@RequiredArgsConstructor
public class FileServiceImpl implements IFileService {

    private final UserFileClient userFileClient;

    private final JWTUtils jwtUtils;

    private final JWTProperties jwtProperties;


    @Override
    public String getDownloadPrivilege(Long id) {

        Result<DownloadInfoVO> result = userFileClient.getDownloadInfo(id);
        DownloadInfoVO data = result.getData();
        HashMap<String, Object> claims = new HashMap<>();
        claims.put(FILE_DOWNLOAD_PATH_HEADER, data.getDownloadPath());
        claims.put(FILE_DOWNLOAD_USERID_HEADER, data.getUserId());

        return jwtUtils.createToken(claims, jwtProperties.getExpire());
    }


}
