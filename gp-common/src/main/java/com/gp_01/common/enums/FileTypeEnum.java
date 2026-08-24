package com.gp_01.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum FileTypeEnum {

    VIDEO(1, "视频文件"),
    AUDIO(2, "音频文件"),
    IMAGE(3, "图片文件"),
    TEXT(4, "文本文件"),
    DOCUMENT(5, "文档文件"),
    ARCHIVE(6, "压缩包文件"),
    OTHER(7, "其他文件");


    @EnumValue
    @JsonValue
    private final Integer value;
    private final String description;

    private static final List<String> archiveList = List.of(
            "application/zip",
            "application/gzip",
            "application/x-7z-compressed",
            "application/x-rar-compressed",
            "application/x-zip-compressed",
            "application/x-tar"
    );
    private static final List<String> documentList = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.ms-excel",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );
    private static final List<String> textList = List.of(
            "application/xml",
            "application/json"
            );

    public static FileTypeEnum getFileTypeEnum(String mime) {
        mime = mime.trim().toLowerCase();
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
                for (String type : archiveList) {
                    if (mime.equals(type)) {
                        return ARCHIVE;
                    }
                }
                for (String type : documentList) {
                    if (mime.equals(type)) {
                        return DOCUMENT;
                    }
                }
                for (String type : textList) {
                    if (mime.equals(type)) {
                        return TEXT;
                    }
                }
                return OTHER;
            }
        }
    }


}
