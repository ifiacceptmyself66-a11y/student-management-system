package com.qinghe.sms.controller;

import com.qinghe.sms.common.PageResult;
import com.qinghe.sms.common.Result;
import com.qinghe.sms.dto.StudentQuery;
import com.qinghe.sms.dto.StudentSaveDTO;
import com.qinghe.sms.dto.StudentVO;
import com.qinghe.sms.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public Result<PageResult<StudentVO>> page(@Valid StudentQuery query) {
        return Result.ok(studentService.page(query));
    }

    @GetMapping("/{id}")
    public Result<StudentVO> detail(@PathVariable Long id) {
        return Result.ok(studentService.detail(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody StudentSaveDTO saveDTO) {
        return Result.ok(studentService.create(saveDTO));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody StudentSaveDTO saveDTO) {
        studentService.update(id, saveDTO);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return Result.ok();
    }
}
