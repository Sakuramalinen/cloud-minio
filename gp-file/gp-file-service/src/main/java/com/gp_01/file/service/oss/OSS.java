package com.gp_01.file.service.oss;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.context.annotation.Bean;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class OSS {


    private String region;

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String defaultBucket;

    private String avatarBucket;

    private String tempBucket;

    public BucketManipulator bucketManipulator;

    public FileManipulator fileManipulator;

}
