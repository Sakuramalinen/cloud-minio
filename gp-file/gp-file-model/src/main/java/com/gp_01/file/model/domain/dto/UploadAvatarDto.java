package com.gp_01.file.model.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class UploadAvatarDto {

    private String fileName;

    private String contentType;

    private Long fileSize;

}
