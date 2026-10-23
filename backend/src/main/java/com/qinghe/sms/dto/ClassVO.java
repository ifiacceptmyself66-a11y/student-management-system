package com.qinghe.sms.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClassVO {

    private Long id;

    private String className;

    private String grade;

    private String headTeacher;

    private String remark;

    /** 班内在读 + 离校但未删除的学生总数 */
    private Long studentCount;

    private LocalDateTime createdAt;
}
