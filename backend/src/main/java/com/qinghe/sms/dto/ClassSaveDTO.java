package com.qinghe.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClassSaveDTO {

    @NotBlank(message = "班级名称不能为空")
    @Size(max = 50, message = "班级名称长度不能超过 50")
    private String className;

    @NotBlank(message = "年级不能为空")
    @Size(max = 20, message = "年级长度不能超过 20")
    private String grade;

    @Size(max = 50, message = "班主任姓名长度不能超过 50")
    private String headTeacher;

    @Size(max = 255, message = "备注长度不能超过 255")
    private String remark;
}
