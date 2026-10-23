package com.qinghe.sms.security;

import com.qinghe.sms.entity.Admin;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security 的登录主体。
 * V1 只有管理员一种角色, 角色固定为 ROLE_ADMIN;
 * 后续需要多角色时, 在 admin 表增加 role 字段并在此按库中角色装配权限。
 */
@Getter
public class LoginAdmin implements UserDetails {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final Long id;
    private final String username;
    private final String password;
    private final String nickname;
    private final Integer status;

    public LoginAdmin(Admin admin) {
        this.id = admin.getId();
        this.username = admin.getUsername();
        this.password = admin.getPassword();
        this.nickname = admin.getNickname();
        this.status = admin.getStatus();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(ROLE_ADMIN));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
