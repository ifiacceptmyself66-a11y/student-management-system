package com.qinghe.sms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qinghe.sms.common.BusinessException;
import com.qinghe.sms.common.PageResult;
import com.qinghe.sms.common.ResultCode;
import com.qinghe.sms.dto.ClassStudentCountVO;
import com.qinghe.sms.dto.ClassQuery;
import com.qinghe.sms.dto.ClassSaveDTO;
import com.qinghe.sms.dto.ClassVO;
import com.qinghe.sms.entity.ClassInfo;
import com.qinghe.sms.mapper.ClassInfoMapper;
import com.qinghe.sms.mapper.StudentMapper;
import com.qinghe.sms.service.ClassInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassInfoServiceImpl implements ClassInfoService {

    private final ClassInfoMapper classInfoMapper;
    private final StudentMapper studentMapper;

    @Override
    public PageResult<ClassVO> page(ClassQuery query) {
        LambdaQueryWrapper<ClassInfo> wrapper = Wrappers.<ClassInfo>lambdaQuery()
                .like(StringUtils.hasText(query.getKeyword()), ClassInfo::getClassName, query.getKeyword())
                .eq(StringUtils.hasText(query.getGrade()), ClassInfo::getGrade, query.getGrade())
                .orderByAsc(ClassInfo::getGrade)
                .orderByAsc(ClassInfo::getClassName);

        Page<ClassInfo> page = classInfoMapper.selectPage(new Page<>(query.getPage(), query.getSize()), wrapper);
        Map<Long, Long> countMap = countStudents(page.getRecords());
        List<ClassVO> records = page.getRecords().stream()
                .map(item -> toVO(item, countMap.getOrDefault(item.getId(), 0L)))
                .collect(Collectors.toList());
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public List<ClassVO> options(String keyword) {
        List<ClassInfo> list = classInfoMapper.selectList(Wrappers.<ClassInfo>lambdaQuery()
                .like(StringUtils.hasText(keyword), ClassInfo::getClassName, keyword)
                .orderByAsc(ClassInfo::getGrade)
                .orderByAsc(ClassInfo::getClassName));
        return list.stream().map(item -> toVO(item, null)).collect(Collectors.toList());
    }

    @Override
    public ClassVO detail(Long id) {
        ClassInfo entity = requireClass(id);
        return toVO(entity, studentMapper.countByClassId(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ClassSaveDTO saveDTO) {
        ensureUnique(saveDTO.getGrade(), saveDTO.getClassName(), null);
        ClassInfo entity = new ClassInfo();
        BeanUtils.copyProperties(saveDTO, entity);
        classInfoMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ClassSaveDTO saveDTO) {
        requireClass(id);
        ensureUnique(saveDTO.getGrade(), saveDTO.getClassName(), id);
        ClassInfo entity = new ClassInfo();
        BeanUtils.copyProperties(saveDTO, entity);
        entity.setId(id);
        classInfoMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireClass(id);
        Long studentCount = studentMapper.countByClassId(id);
        if (studentCount != null && studentCount > 0) {
            throw new BusinessException(ResultCode.CLASS_HAS_STUDENT,
                    "该班级下仍有 " + studentCount + " 名学生，请先转移或删除学生");
        }
        classInfoMapper.deleteById(id);
    }

    private ClassInfo requireClass(Long id) {
        ClassInfo entity = classInfoMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "班级不存在");
        }
        return entity;
    }

    /**
     * 同一年级下班级名称不可重复(逻辑删除的记录不参与校验)。
     */
    private void ensureUnique(String grade, String className, Long excludeId) {
        Long count = classInfoMapper.selectCount(Wrappers.<ClassInfo>lambdaQuery()
                .eq(ClassInfo::getGrade, grade)
                .eq(ClassInfo::getClassName, className)
                .ne(excludeId != null, ClassInfo::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.DATA_CONFLICT, "该年级下已存在同名班级");
        }
    }

    private Map<Long, Long> countStudents(List<ClassInfo> classes) {
        if (classes == null || classes.isEmpty()) {
            return Map.of();
        }
        List<Long> classIds = classes.stream().map(ClassInfo::getId).collect(Collectors.toList());
        return studentMapper.countGroupByClassIds(classIds).stream()
                .collect(Collectors.toMap(ClassStudentCountVO::getClassId, ClassStudentCountVO::getTotal));
    }

    private ClassVO toVO(ClassInfo entity, Long studentCount) {
        ClassVO vo = new ClassVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setStudentCount(studentCount);
        return vo;
    }
}
