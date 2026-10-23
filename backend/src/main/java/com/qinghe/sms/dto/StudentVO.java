package com.qinghe.sms.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudentVO {

    private Long id;

    private String studentNo;

    private String name;

    private Integer gender;

    private LocalDate birthDate;

    /** 由 birthDate 实时计算, 不落库 */
    private Integer age;

    private String phone;

    private String email;

    private String guardianName;

    private String guardianPhone;

    private Long classId;

    private String className;

    private String grade;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
