package com.qinghe.sms.service;

import com.qinghe.sms.common.PageResult;
import com.qinghe.sms.dto.StudentQuery;
import com.qinghe.sms.dto.StudentSaveDTO;
import com.qinghe.sms.dto.StudentVO;

public interface StudentService {

    PageResult<StudentVO> page(StudentQuery query);

    StudentVO detail(Long id);

    Long create(StudentSaveDTO saveDTO);

    void update(Long id, StudentSaveDTO saveDTO);

    void delete(Long id);
}
