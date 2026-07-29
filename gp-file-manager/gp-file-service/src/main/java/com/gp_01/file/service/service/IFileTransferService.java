package com.gp_01.file.service.service;

import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.file.model.domain.dto.PreviewFileDTO;
import com.gp_01.file.model.domain.dto.UploadAuthorizationDTO;
import com.gp_01.file.model.domain.vo.PreviewImagesVO;
import com.gp_01.file.model.domain.vo.UploadFileVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public interface IFileTransferService {

    String directionConnectionDownload(Long id);

    UploadFileVO uploadAuthorize(UploadAuthorizationDTO dto);

    Map<Integer, String> directConnectionChunkUploadFile(Long taskId, List<Integer> chunkNumbers);

    void uploadChunkFileMerge(@NotBlank Long taskId);

    String directConnectionWholeUploadFile(Long taskId);

    String directionConnectionPreview(@Valid PreviewFileDTO dto);

    PageResult<PreviewImagesVO> previewThumbnailsPage(PageParams params);

    void saveUploadFile( Long taskId);
}
