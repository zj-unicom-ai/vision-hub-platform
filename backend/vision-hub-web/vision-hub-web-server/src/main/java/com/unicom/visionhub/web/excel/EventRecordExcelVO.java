package com.unicom.visionhub.web.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;


@Data
public class EventRecordExcelVO {

    @ExcelProperty("事件名称")
    @ColumnWidth(20)
    private String eventName;

    @ExcelProperty("事件时间")
    @ColumnWidth(20)
    private String createdAt;

    @ExcelProperty("点位名称")
    @ColumnWidth(31)
    private String deviceName;
    @ExcelProperty("任务名称")
    @ColumnWidth(31)
    private String taskName;

    @ExcelProperty("任务算法")
    @ColumnWidth(31)
    private String algorithmName;
}
