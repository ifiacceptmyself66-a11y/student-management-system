package com.qinghe.sms.controller;

import com.qinghe.sms.common.Result;
import com.qinghe.sms.dto.AdminVO;
import com.qinghe.sms.dto.LoginDTO;
import com.qinghe.sms.dto.LoginVO;
import com.qinghe.sms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.ok(authService.login(loginDTO));
    }

    @GetMapping("/me")
    public Result<AdminVO> me() {
        return Result.ok(authService.currentAdmin());
    }

    /**
     * JWT 无状态, 退出登录由前端清除本地 token 即可; 此接口仅用于统一前端调用。
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok();
    }
}
