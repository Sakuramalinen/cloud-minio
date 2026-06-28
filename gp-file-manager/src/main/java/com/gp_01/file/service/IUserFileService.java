package com.gp_01.file.service;

import com.gp_01.common.domain.Result;
import com.gp_01.common.domain.dto.PageResult;
import com.gp_01.common.domain.query.PageParams;
import com.gp_01.file.domain.dto.UploadFileDTO;
import com.gp_01.file.domain.po.UserFile;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gp_01.file.domain.query.PageFilesQuery;
import com.gp_01.file.domain.vo.ListRecycleBinVO;
import com.gp_01.file.domain.vo.PreviewImagesVO;
import com.gp_01.file.domain.vo.UploadVO;
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
    @Deprecated
    void uploadFile(MultipartFile file, Long parentId, String md5Hex);

    void makeDir(Long parentId, String fileName);

    void reName(Long id, String fileName);

    void deleteById(Long id);

    PageResult<UserFile> listFileByParentId(PageFilesQuery pageFilesQuery);

    /**
     * 下载单个文件
     * @param id
     * @param response
     */
    void downloadById(Long id, HttpServletResponse response);

    void previewFileById(String id, HttpServletResponse response);

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
}
