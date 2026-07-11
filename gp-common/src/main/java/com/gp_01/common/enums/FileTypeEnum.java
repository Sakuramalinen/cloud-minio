package com.gp_01.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileTypeEnum {

    OTHER(5, "其他文件"),
    DIRECTORY(0, "文件夹"),
    VIDEO(1, "视频文件"),
    AUDIO(2, "音频文件"),
    IMAGE(3, "图片文件"),
    TEXT(4, "文本文件");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String description;


    public static FileTypeEnum getFileTypeEnum(String mime) {
        String prefix = mime.split("/")[0];
        switch (prefix) {
            case "video" -> {
                return VIDEO;
            }
            case "audio" -> {
                return AUDIO;
            }
            case "image" -> {
                return IMAGE;
            }
            case "text" -> {
                return TEXT;
            }
            default -> {
                return OTHER;
            }
        }
    }


}
