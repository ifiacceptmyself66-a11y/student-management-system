package com.qinghe.sms.controller;

import com.qinghe.sms.common.PageResult;
import com.qinghe.sms.common.Result;
import com.qinghe.sms.dto.ClassQuery;
import com.qinghe.sms.dto.ClassSaveDTO;
import com.qinghe.sms.dto.ClassVO;
import com.qinghe.sms.service.ClassInfoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassInfoController {

    private final ClassInfoService classInfoService;

    @GetMapping
    public Result<PageResult<ClassVO>> page(@Valid ClassQuery query) {
        return Result.ok(classInfoService.page(query));
    }

    @GetMapping("/options")
    public Result<List<ClassVO>> options(@RequestParam(required = false) String keyword) {
        return Result.ok(classInfoService.options(keyword));
    }

    @GetMapping("/{id}")
    public Result<ClassVO> detail(@PathVariable Long id) {
        return Result.ok(classInfoService.detail(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody ClassSaveDTO saveDTO) {
        return Result.ok(classInfoService.create(saveDTO));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ClassSaveDTO saveDTO) {
        classInfoService.update(id, saveDTO);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        classInfoService.delete(id);
        return Result.ok();
    }
}
