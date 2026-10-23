package com.qinghe.sms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页参数基类, 默认每页 10 条, 可选 10/20/50。
 */
@Data
public class PageQuery {

    @Min(value = 1, message = "页码不能小于 1")
    private long page = 1;

    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 200, message = "每页条数不能大于 200")
    private long size = 10;
}
