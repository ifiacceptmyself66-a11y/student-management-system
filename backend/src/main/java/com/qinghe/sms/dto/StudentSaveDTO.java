package com.qinghe.sms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentSaveDTO {

    @NotBlank(message = "学号不能为空")
    @Size(max = 20, message = "学号长度不能超过 20")
    private String studentNo;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过 50")
    private String name;

    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别取值不合法")
    @Max(value = 2, message = "性别取值不合法")
    private Integer gender;

    @Past(message = "出生日期必须是过去的日期")
    private LocalDate birthDate;

    @Pattern(regexp = "^$|^\\d{6,20}$", message = "学生手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100")
    private String email;

    @Size(max = 50, message = "家长姓名长度不能超过 50")
    private String guardianName;

    @Pattern(regexp = "^$|^\\d{6,20}$", message = "家长联系电话格式不正确")
    private String guardianPhone;

    @NotNull(message = "所属班级不能为空")
    private Long classId;

    @Min(value = 0, message = "状态取值不合法")
    @Max(value = 1, message = "状态取值不合法")
    private Integer status = 1;
}
