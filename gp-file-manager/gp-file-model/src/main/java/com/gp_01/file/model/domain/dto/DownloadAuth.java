package com.gp_01.file.model.domain.dto;

import lombok.Data;

@Data
public class DownloadAuth {

    public String downloadPath;

    public String contentType;

    public String fileName;
}
