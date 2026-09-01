package com.gp_01.file.service.oss;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Data
@Accessors(chain = true)
public class OSS {
    //默认上传桶
    private String bucketName;
    //连接地址
    private String url;
    //头像桶
    private String avatarBucketName;
    //临时文件通
    private String tempBucketName;
    //许可
    private String accessKey;
    //密钥
    private String secretKey;

}
