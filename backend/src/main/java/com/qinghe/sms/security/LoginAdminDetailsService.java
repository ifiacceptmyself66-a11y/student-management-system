package com.qinghe.sms.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qinghe.sms.entity.Admin;
import com.qinghe.sms.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginAdminDetailsService implements UserDetailsService {

    private final AdminMapper adminMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminMapper.selectOne(Wrappers.<Admin>lambdaQuery().eq(Admin::getUsername, username));
        if (admin == null) {
            throw new UsernameNotFoundException("账号不存在");
        }
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new DisabledException("账号已被禁用");
        }
        return new LoginAdmin(admin);
    }
}
