package com.qinghe.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank(message = "账号不能为空")
    @Size(max = 50, message = "账号长度不能超过 50")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(max = 64, message = "密码长度不能超过 64")
    private String password;
}
