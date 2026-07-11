package com.gp_01.api.client;

import com.gp_01.common.domain.Result;
import com.gp_01.file.model.domain.vo.DownloadInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient("gp-file-service")
public interface UserFileClient {


    @GetMapping("file-transfer/download/info/{id}")
    Result<DownloadInfoVO> getDownloadInfo(@PathVariable Long id);
}
