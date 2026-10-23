package com.qinghe.sms.service.impl;

import com.qinghe.sms.common.BusinessException;
import com.qinghe.sms.common.ResultCode;
import com.qinghe.sms.dto.AdminVO;
import com.qinghe.sms.dto.LoginDTO;
import com.qinghe.sms.dto.LoginVO;
import com.qinghe.sms.security.JwtUtil;
import com.qinghe.sms.security.LoginAdmin;
import com.qinghe.sms.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
        } catch (DisabledException e) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        } catch (InternalAuthenticationServiceException e) {
            // DaoAuthenticationProvider 会把数据访问层的异常统一包装成此类型。
            // 必须按系统异常返回: 否则数据库不可用时用户只会看到"用户名或密码错误", 既误导也无法排查。
            log.error("登录过程数据访问失败, username={}", loginDTO.getUsername(), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "登录服务暂时不可用，请稍后重试");
        } catch (AuthenticationException e) {
            log.debug("登录失败, username={}", loginDTO.getUsername());
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        LoginAdmin admin = (LoginAdmin) authentication.getPrincipal();
        String token = jwtUtil.generate(admin);
        return new LoginVO(token, jwtUtil.getExpireSeconds(),
                new AdminVO(admin.getId(), admin.getUsername(), admin.getNickname()));
    }

    @Override
    public AdminVO currentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginAdmin admin)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return new AdminVO(admin.getId(), admin.getUsername(), admin.getNickname());
    }
}
