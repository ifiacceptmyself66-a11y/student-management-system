package com.qinghe.sms.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClassQuery extends PageQuery {

    /** 班级名称模糊匹配 */
    private String keyword;

    /** 年级精确匹配 */
    private String grade;
}
