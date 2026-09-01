package com.gp_01.file.model.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UploadPreSignDTO {
    //是否分片上传
    @NotNull
    private Boolean IsChunked;

    private List<Integer> chunkNumbers;

}
