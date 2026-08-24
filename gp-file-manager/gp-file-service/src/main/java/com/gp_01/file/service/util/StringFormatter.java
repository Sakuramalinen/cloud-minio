package com.gp_01.file.service.util;

import java.util.Formatter;

public class StringFormatter {
    /**
     * 下载凭证缓存key格式化器
     * @param userId 用户id
     * @param ticket 凭证
     * @return 缓存key
     */
    public static String fileDownloadTicketCacheKeyFormatter(Long userId, String ticket){
        return String.format("gp_01:file:download:ticket:%d:%s", userId, ticket);
    }
}
