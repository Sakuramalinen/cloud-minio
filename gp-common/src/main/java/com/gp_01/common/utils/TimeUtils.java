package com.gp_01.common.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class TimeUtils  {

    /**
     * 毫秒级时间戳转LocalDateTime
     * @param milli
     * @return
     */
    public static LocalDateTime milliToLocalDateTime(Long milli){
        if(milli == null)return null;
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(milli), ZoneId.of("Asia/Shanghai"));
    }

    /**
     * 秒级时间戳转LocalDateTime
     * @param second
     * @return
     */
    public static LocalDateTime secondToLocalDateTime(Long second){
        if(second == null)return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(second), ZoneId.of("Asia/Shanghai"));
    }



}
