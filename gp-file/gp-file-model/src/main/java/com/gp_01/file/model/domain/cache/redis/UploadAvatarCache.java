package com.gp_01.file.model.domain.cache.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
public class UploadAvatarCache {
    private String objectPath;
    private String fileName;
    private String contentType;
    private Long fileSize;
}
