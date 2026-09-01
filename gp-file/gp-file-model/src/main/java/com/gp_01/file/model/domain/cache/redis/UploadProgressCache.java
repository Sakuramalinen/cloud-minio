package com.gp_01.file.model.domain.cache.redis;

import lombok.Data;

@Data
public class UploadProgressCache {

    private String chunkBitmap;

    private Integer status;
}
