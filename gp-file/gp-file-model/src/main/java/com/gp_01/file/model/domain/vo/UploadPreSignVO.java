package com.gp_01.file.model.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class UploadPreSignVO {

    private Map<Integer, String> chunkPreSignUrls;

    private String preSignUrl;


    public UploadPreSignVO(Map<Integer, String> chunkPreSignUrls) {
        this.chunkPreSignUrls = chunkPreSignUrls;
    }

    public UploadPreSignVO(String preSignUrl) {
        this.preSignUrl = preSignUrl;
    }
}
