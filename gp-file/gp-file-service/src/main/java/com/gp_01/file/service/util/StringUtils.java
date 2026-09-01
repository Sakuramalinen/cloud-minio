package com.gp_01.file.service.util;

public class StringUtils {

    public static String ContentRangeFormat(Long start, Long end, Long total){
        return String.format("bytes %d-%d/%d", start, end, total);
    }
}
