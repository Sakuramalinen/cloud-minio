package com.gp_01.auth.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gp_01.auth.model.dto.LoginFormDTO;
import com.gp_01.auth.model.dto.RegisterDTO;
import com.gp_01.auth.model.po.Account;
import com.gp_01.auth.model.vo.LoginVO;

public interface IAccountService extends IService<Account> {
    LoginVO login(LoginFormDTO loginFormDTO);

    void register(RegisterDTO registerDTO);
}
