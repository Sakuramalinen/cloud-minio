package com.gp_01.common.context;

import com.gp_01.common.domain.header.FileDownloadHeaderParam;

public class FileDownloadContext {
    private static final ThreadLocal<String> tl = new ThreadLocal<>();

    private FileDownloadContext() {
    }


    public static String getDownloadPath() {
        return tl.get();
    }

    public static void setDownloadPath(String path) {
        tl.set(path);
    }

    public static void removeDownloadPath() {
        tl.remove();
    }

}
