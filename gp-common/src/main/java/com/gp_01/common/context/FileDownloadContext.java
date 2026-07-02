package com.gp_01.common.context;

import com.gp_01.common.domain.dto.FileDownloadDTO;

public class FileDownloadContext {
    private static final ThreadLocal<FileDownloadDTO> tl = new ThreadLocal<>();

    private FileDownloadContext() {
    }


    public static String getDownloadPath() {
        return tl.get().getDownloadPath();
    }

    public static Long getDownloadUser(){
        return tl.get().getUserId();
    }

    public static void set(FileDownloadDTO dto) {
        tl.set(dto);
    }

    public static void set(String downloadPath, Long downloadUserId){
        tl.set(new FileDownloadDTO(downloadPath, downloadUserId));
    }

    public static void removeDownloadPath() {
        tl.remove();
    }

}
