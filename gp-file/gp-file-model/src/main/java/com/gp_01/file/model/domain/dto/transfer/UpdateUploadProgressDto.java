package com.gp_01.file.model.domain.dto.transfer;

import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateUploadProgressDto {

//    @NotNull
//    @SchemaProperty(name = "上传id")
//    private String uploadId;

    @NotNull
    @SchemaProperty(name = "分片序号")
    private Long chunkNumber;
}
