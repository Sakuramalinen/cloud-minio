package com.gp_01.file.service.service;

import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.file.model.domain.dto.DownloadFileDTO;
import com.gp_01.file.model.domain.dto.PreviewFileDTO;
import com.gp_01.file.model.domain.dto.UploadFileDTO;
import com.gp_01.file.model.domain.vo.DownloadInfoVO;
import com.gp_01.file.model.domain.vo.PreviewImagesVO;
import com.gp_01.file.model.domain.vo.UploadVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public interface IFileTransferService {
    UploadVO uploadFile(@NotNull MultipartFile file, @Valid UploadFileDTO uploadFileDTO);


    void downloadFile(HttpServletRequest request, HttpServletResponse response, @Valid DownloadFileDTO dto);

    DownloadInfoVO getDownloadInfo(Long id);

    void previewFile(HttpServletRequest request, HttpServletResponse response, @Valid PreviewFileDTO dto);

    PageResult<PreviewImagesVO> pagePreviewImages(@Valid PageParams params);

}
