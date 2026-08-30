package com.gp_01.common.context;

import com.gp_01.common.domain.context.UploadInfo;

public class UploadInfoContext {

    private static final ThreadLocal<UploadInfo> TL = new ThreadLocal<>();

    private UploadInfoContext() {
    }

    /**
     * 保存上传id
     *
     * @param uploadInfo
     */
    public static void setUploadInfo(UploadInfo uploadInfo) {
        TL.set(uploadInfo);
    }

    /**
     * 获取上传id
     *
     * @return
     */
    public static UploadInfo getUploadInfo() {
        return TL.get();
    }

    /**
     * 删除上传id
     */
    public static void removeUploadInfo() {
        TL.remove();
    }
}
