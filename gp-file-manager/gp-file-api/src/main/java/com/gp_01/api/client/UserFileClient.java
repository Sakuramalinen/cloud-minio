package com.gp_01.api.client;

import com.gp_01.common.domain.Result;
import com.gp_01.file.model.domain.vo.DownloadPrivilegeVO;
import com.gp_01.file.model.domain.vo.FileDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient("gp-file-service")
public interface UserFileClient {

//    @GetMapping("user-file/download/detail/{id}")
//    Result<FileDetail> getFileDetail(@PathVariable Long id);

    @GetMapping("user-file/download/path/{id}")
    Result<String> getDownloadPath(@PathVariable Long id);
}
