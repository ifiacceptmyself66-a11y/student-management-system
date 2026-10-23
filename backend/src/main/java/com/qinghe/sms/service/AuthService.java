package com.qinghe.sms.service;

import com.qinghe.sms.dto.AdminVO;
import com.qinghe.sms.dto.LoginDTO;
import com.qinghe.sms.dto.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO loginDTO);

    AdminVO currentAdmin();
}
