package com.gp_01.auth.service;

import com.gp_01.model.domain.dto.LoginFormDTO;

public interface IAccountService {
    String login(LoginFormDTO loginFormDTO);
}
