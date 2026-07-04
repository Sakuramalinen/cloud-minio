package com.gp_01.file.service.service;

import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.file.model.domain.dto.DownloadFileDTO;
import com.gp_01.file.model.domain.dto.PreviewFileDTO;
import com.gp_01.file.model.domain.dto.UploadFileDTO;
import com.gp_01.file.model.domain.po.UserFile;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gp_01.file.model.domain.query.PageFilesQuery;
import com.gp_01.file.model.domain.vo.FileDetail;
import com.gp_01.file.model.domain.vo.ListRecycleBinVO;
import com.gp_01.file.model.domain.vo.PreviewImagesVO;
import com.gp_01.file.model.domain.vo.UploadVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 用户逻辑文件表 服务类
 * </p>
 *
 * @author employee_01
 * @since 2026-05-08
 */
public interface IUserFileService extends IService<UserFile> {

//    void uploadFile(MultipartFile file, Long parentId, String md5Hex);

    void makeDir(Long parentId, String fileName);

    void reName(Long id, String fileName);

    void deleteById(Long id);

    PageResult<UserFile> listFileByParentId(PageFilesQuery pageFilesQuery);

    /**
     * 下载单个文件
     * @param id
     * @param response
     */
//    void downloadById(Long id, HttpServletResponse response);

//    void previewFileById(String id, HttpServletResponse response);

    /**
     * 查看回收站
     * @return
     */
    List<ListRecycleBinVO> listRecycleBin();

    /**
     * 从回收站恢复文件
     * @param ids
     */
    void restoreFile(List<Long> ids);


    PageResult<PreviewImagesVO> pagePreviewImages(PageParams params);

    PageResult<UserFile> listFileByTypeAndPage(PageParams params, Integer type);

    void moveFile(Long fileId, Long targetId);

    List<UserFile> listDirByParentId(Long id);

    UploadVO uploadFile(MultipartFile file, UploadFileDTO uploadFileDTO);

    void downloadFile(HttpServletRequest request, HttpServletResponse response, DownloadFileDTO dto);


    FileDetail getFileDetail(Long id);

    /**
     * 批量删除回收站文件
     * @param ids
     */
    void deleteRecycleFileBatch(List<Long> ids);

    void previewFile(HttpServletRequest request, HttpServletResponse response, PreviewFileDTO dto);
}
