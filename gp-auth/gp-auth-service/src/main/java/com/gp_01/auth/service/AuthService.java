package com.gp_01.auth.service;

import com.gp_01.model.domain.dto.LoginFormDTO;
import com.gp_01.model.domain.po.User;

public interface AuthService {

    User execute(LoginFormDTO loginFormDTO);
}
