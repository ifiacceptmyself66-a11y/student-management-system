package com.qinghe.sms.service;

import com.qinghe.sms.common.PageResult;
import com.qinghe.sms.dto.ClassQuery;
import com.qinghe.sms.dto.ClassSaveDTO;
import com.qinghe.sms.dto.ClassVO;

import java.util.List;

public interface ClassInfoService {

    PageResult<ClassVO> page(ClassQuery query);

    List<ClassVO> options(String keyword);

    ClassVO detail(Long id);

    Long create(ClassSaveDTO saveDTO);

    void update(Long id, ClassSaveDTO saveDTO);

    void delete(Long id);
}
