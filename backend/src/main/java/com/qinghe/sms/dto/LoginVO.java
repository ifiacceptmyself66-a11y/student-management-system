package com.qinghe.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginVO {

    private String token;

    /** token 有效期, 单位秒 */
    private long expiresIn;

    private AdminVO admin;
}
