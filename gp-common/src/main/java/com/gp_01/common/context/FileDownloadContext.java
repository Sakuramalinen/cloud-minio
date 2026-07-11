package com.gp_01.common.context;

import com.gp_01.common.domain.dto.FileDownloadDTO;

public class FileDownloadContext {
    private static final ThreadLocal<FileDownloadDTO> tl = new ThreadLocal<>();

    private FileDownloadContext() {
    }

    public static Long getUserId(){
        return tl.get().getUserId();
    }
    public static String getStorePath(){
        return tl.get().getStorePath();
    }

    public static void set(FileDownloadDTO dto) {
        tl.set(dto);
    }

    public static void set(String storePath, Long userId){
        tl.set(new FileDownloadDTO(storePath, userId));
    }

    public static void removeDownloadPath() {
        tl.remove();
    }

}
