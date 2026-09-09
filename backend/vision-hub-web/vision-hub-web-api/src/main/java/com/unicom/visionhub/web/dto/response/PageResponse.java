package com.unicom.visionhub.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.io.Serial;


@Data
@NoArgsConstructor
@Schema(description = "分页响应结果")
public class PageResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "当前页码")
    private Integer pageNum;


    @Schema(description = "每页条数")
    private Integer pageSize;


    @Schema(description = "总记录数")
    private Long total;


    @Schema(description = "总页数")
    private Integer totalPages;


    @Schema(description = "数据列表")
    private List<T> records;


    @Schema(description = "是否有下一页")
    private Boolean hasNext;


    @Schema(description = "是否有上一页")
    private Boolean hasPrevious;


    public static <T> PageResponse<T> of(Integer pageNum, Integer pageSize, Long total, List<T> records) {
        PageResponse<T> response = new PageResponse<>();
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setTotal(total);
        response.setRecords(records);
        response.setTotalPages((int) Math.ceil((double) total / pageSize));
        response.setHasNext(pageNum < response.getTotalPages());
        response.setHasPrevious(pageNum > 1);
        return response;
    }


    public static <T> PageResponse<T> empty() {
        PageResponse<T> response = new PageResponse<>();
        response.setPageNum(1);
        response.setPageSize(10);
        response.setTotal(0L);
        response.setTotalPages(0);
        response.setRecords(new ArrayList<T>());
        response.setHasNext(false);
        response.setHasPrevious(false);
        return response;
    }
}
