package com.gp_01.file.model.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UploadProgressSaveDTO {

    @NotNull
    private Long taskId;

    private String chunkBitMap;

    private Integer status;

}
