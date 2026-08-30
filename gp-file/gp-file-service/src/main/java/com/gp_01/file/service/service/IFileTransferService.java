package com.gp_01.file.service.service;

import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.file.model.domain.dto.*;
import com.gp_01.file.model.domain.vo.PreviewImagesVO;
import com.gp_01.file.model.domain.vo.UploadFileVO;
import com.gp_01.file.model.domain.vo.UploadPreSignVO;
import jakarta.validation.Valid;

public interface IFileTransferService {

    UploadFileVO uploadAuthorize(UploadAuthorizationDTO dto);

    UploadPreSignVO getUploadPreSignedUrl(UploadPreSignDTO dto);

    void uploadComplete(UploadCompleteDTO dto);

    String downloadFile(Long id);

    String previewFile(Long userFileId);

    PageResult<PreviewImagesVO> previewThumbnailsPage(PageParams params);

    void uploadFilePostHandle(UploadFilePostHandleDTO dto);


}
