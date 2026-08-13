package com.gp_01.file.model.domain.cache.redis;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UploadFileCache {

    private String bucketName;

    private String objectPath;

    private String uploadId;


}
