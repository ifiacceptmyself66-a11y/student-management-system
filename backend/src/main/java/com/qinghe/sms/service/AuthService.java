package com.qinghe.sms.service;

import com.qinghe.sms.dto.AdminVO;
import com.qinghe.sms.dto.ChangePasswordDTO;
import com.qinghe.sms.dto.LoginDTO;
import com.qinghe.sms.dto.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO loginDTO);

    AdminVO currentAdmin();

    /**
     * 修改当前登录管理员的密码, 需要校验原密码。
     */
    void changePassword(ChangePasswordDTO changePasswordDTO);
}
