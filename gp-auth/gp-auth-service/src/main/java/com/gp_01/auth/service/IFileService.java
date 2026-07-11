package com.gp_01.auth.service;

import jakarta.validation.constraints.NotNull;

public interface IFileService {


    String getDownloadPrivilege(Long id);

}
