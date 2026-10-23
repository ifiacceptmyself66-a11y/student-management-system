package com.qinghe.sms.common;

import lombok.Getter;

/**
 * 业务状态码。接口 HTTP 状态码只区分 200/401/403/500, 业务结果由 code 表达。
 */
@Getter
public enum ResultCode {

    SUCCESS(0, "成功"),
    PARAM_ERROR(1001, "参数校验失败"),
    UNAUTHORIZED(1002, "未登录或登录已过期"),
    FORBIDDEN(1003, "没有操作权限"),

    LOGIN_FAILED(2001, "用户名或密码错误"),
    STUDENT_NO_EXISTS(2002, "学号已存在"),
    CLASS_HAS_STUDENT(2003, "该班级下仍有学生，无法删除"),
    NOT_FOUND(2004, "数据不存在"),
    ACCOUNT_DISABLED(2005, "账号已被禁用"),
    DATA_CONFLICT(2006, "数据已存在，请检查唯一字段"),
    OLD_PASSWORD_ERROR(2007, "原密码不正确"),
    WEAK_PASSWORD(2008, "新密码过于简单，请更换"),

    SERVER_ERROR(5000, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
