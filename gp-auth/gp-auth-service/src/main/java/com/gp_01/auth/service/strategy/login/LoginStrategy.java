package com.gp_01.auth.service.strategy.login;

import com.gp_01.auth.model.dto.LoginFormDTO;
import com.gp_01.auth.model.enums.LoginType;
import com.gp_01.auth.model.po.Account;

public interface LoginStrategy {

    LoginType supportedType();

    Account login(LoginFormDTO loginForm);
}
