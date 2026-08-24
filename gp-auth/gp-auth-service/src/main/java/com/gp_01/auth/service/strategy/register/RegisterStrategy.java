package com.gp_01.auth.service.strategy.register;

import com.gp_01.auth.model.dto.RegisterDTO;
import com.gp_01.auth.model.enums.RegisterType;
import com.gp_01.auth.model.po.Account;

public interface RegisterStrategy {

    RegisterType supportedType();

    Account register(RegisterDTO registerDTO);
}
