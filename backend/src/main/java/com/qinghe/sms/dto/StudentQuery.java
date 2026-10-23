package com.qinghe.sms.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentQuery extends PageQuery {

    /** 同时模糊匹配姓名与学号 */
    private String keyword;

    private Long classId;

    /** 0 未知, 1 男, 2 女 */
    private Integer gender;

    /** 1 在读, 0 离校 */
    private Integer status;
}
