package com.qinghe.sms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qinghe.sms.dto.ClassStudentCountVO;
import com.qinghe.sms.dto.StudentQuery;
import com.qinghe.sms.dto.StudentVO;
import com.qinghe.sms.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    /**
     * 学生分页列表, 联表带出班级名称与年级。
     */
    IPage<StudentVO> selectStudentPage(IPage<StudentVO> page, @Param("query") StudentQuery query);

    /**
     * 批量统计多个班级下的学生数, 供班级列表使用。
     */
    List<ClassStudentCountVO> countGroupByClassIds(@Param("classIds") Collection<Long> classIds);

    /**
     * 统计单个班级下的学生数, 删除班级前校验使用。
     */
    Long countByClassId(@Param("classId") Long classId);
}
