package com.qinghe.sms.service.impl;

import com.qinghe.sms.common.BusinessException;
import com.qinghe.sms.common.ResultCode;
import com.qinghe.sms.dto.AdminVO;
import com.qinghe.sms.dto.ChangePasswordDTO;
import com.qinghe.sms.dto.LoginDTO;
import com.qinghe.sms.dto.LoginVO;
import com.qinghe.sms.entity.Admin;
import com.qinghe.sms.mapper.AdminMapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** 弱密码黑名单: 阻止把默认口令 123456 换成另一个同样弱的口令 */
    private static final List<String> WEAK_PASSWORDS = List.of(
            "123456", "1234567", "12345678", "123456789", "111111", "000000", "123123",
            "abc123", "a123456", "admin", "admin123", "password", "qwerty", "sms123456");

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

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
        LoginAdmin admin = currentLoginAdmin();
        return new AdminVO(admin.getId(), admin.getUsername(), admin.getNickname());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        LoginAdmin loginAdmin = currentLoginAdmin();
        Admin entity = adminMapper.selectById(loginAdmin.getId());
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "账号不存在");
        }

        String oldPassword = changePasswordDTO.getOldPassword();
        String newPassword = changePasswordDTO.getNewPassword();

        if (!passwordEncoder.matches(oldPassword, entity.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        if (newPassword.equals(oldPassword)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "新密码不能与原密码相同");
        }
        if (isWeakPassword(newPassword)) {
            throw new BusinessException(ResultCode.WEAK_PASSWORD);
        }

        Admin update = new Admin();
        update.setId(entity.getId());
        update.setPassword(passwordEncoder.encode(newPassword));
        adminMapper.updateById(update);
        log.info("管理员修改密码成功, adminId={}, username={}", entity.getId(), entity.getUsername());
    }

    private LoginAdmin currentLoginAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginAdmin admin)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return admin;
    }

    private boolean isWeakPassword(String password) {
        String lower = password.toLowerCase(Locale.ROOT);
        return WEAK_PASSWORDS.contains(lower);
    }
}
