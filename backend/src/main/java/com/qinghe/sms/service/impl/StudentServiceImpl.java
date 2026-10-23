package com.qinghe.sms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qinghe.sms.common.BusinessException;
import com.qinghe.sms.common.PageResult;
import com.qinghe.sms.common.ResultCode;
import com.qinghe.sms.dto.StudentQuery;
import com.qinghe.sms.dto.StudentSaveDTO;
import com.qinghe.sms.dto.StudentVO;
import com.qinghe.sms.entity.ClassInfo;
import com.qinghe.sms.entity.Student;
import com.qinghe.sms.mapper.ClassInfoMapper;
import com.qinghe.sms.mapper.StudentMapper;
import com.qinghe.sms.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;
    private final ClassInfoMapper classInfoMapper;

    @Override
    public PageResult<StudentVO> page(StudentQuery query) {
        Page<StudentVO> page = new Page<>(query.getPage(), query.getSize());
        IPage<StudentVO> result = studentMapper.selectStudentPage(page, query);
        result.getRecords().forEach(vo -> vo.setAge(calcAge(vo.getBirthDate())));
        return PageResult.of(result);
    }

    @Override
    public StudentVO detail(Long id) {
        Student entity = requireStudent(id);
        StudentVO vo = new StudentVO();
        BeanUtils.copyProperties(entity, vo);
        ClassInfo classInfo = classInfoMapper.selectById(entity.getClassId());
        if (classInfo != null) {
            vo.setClassName(classInfo.getClassName());
            vo.setGrade(classInfo.getGrade());
        }
        vo.setAge(calcAge(entity.getBirthDate()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(StudentSaveDTO saveDTO) {
        String studentNo = normalize(saveDTO.getStudentNo());
        saveDTO.setStudentNo(studentNo);
        ensureStudentNoUnique(studentNo, null);
        requireClass(saveDTO.getClassId());

        Student entity = new Student();
        BeanUtils.copyProperties(saveDTO, entity);
        studentMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, StudentSaveDTO saveDTO) {
        requireStudent(id);
        String studentNo = normalize(saveDTO.getStudentNo());
        saveDTO.setStudentNo(studentNo);
        ensureStudentNoUnique(studentNo, id);
        requireClass(saveDTO.getClassId());

        Student entity = new Student();
        BeanUtils.copyProperties(saveDTO, entity);
        entity.setId(id);
        studentMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireStudent(id);
        studentMapper.deleteById(id);
    }

    private Student requireStudent(Long id) {
        Student entity = studentMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "学生不存在");
        }
        return entity;
    }

    private void requireClass(Long classId) {
        ClassInfo classInfo = classInfoMapper.selectById(classId);
        if (classInfo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "所属班级不存在");
        }
    }

    private void ensureStudentNoUnique(String studentNo, Long excludeId) {
        Long count = studentMapper.selectCount(Wrappers.<Student>lambdaQuery()
                .eq(Student::getStudentNo, studentNo)
                .ne(excludeId != null, Student::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.STUDENT_NO_EXISTS);
        }
    }

    private Integer calcAge(LocalDate birthDate) {
        if (birthDate == null) {
            return null;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : value;
    }
}
