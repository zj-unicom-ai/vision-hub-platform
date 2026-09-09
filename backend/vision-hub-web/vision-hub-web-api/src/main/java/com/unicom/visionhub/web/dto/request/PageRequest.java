package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.io.Serial;


@Data
@Schema(description = "分页请求基类")
public class PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int MAX_PAGE_SIZE = 100;


    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = DEFAULT_PAGE_NUM;


    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = DEFAULT_PAGE_SIZE;


    @Schema(description = "排序字段")
    private String sortField;


    @Schema(description = "排序方向", example = "desc")
    private String sortDirection;


    public Integer getPageNum() {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }


    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }


    public int getOffset() {
        return (getPageNum() - 1) * getPageSize();
    }
}
