package com.qinghe.sms.dto;

import lombok.Data;

/**
 * 按班级分组的学生数量, 用于班级列表批量统计, 避免 N+1 查询。
 */
@Data
public class ClassStudentCountVO {

    private Long classId;

    private Long total;
}
