package com.unicom.visionhub.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.visionhub.web.dto.request.TaskDeviceQueryRequest;
import com.unicom.visionhub.web.entity.task.TaskDevice;
import com.unicom.visionhub.web.vo.TaskDeviceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface TaskDeviceMapper extends BaseMapper<TaskDevice> {


    IPage<TaskDeviceVO> selectPageByTaskIdAndAlgorithmId(Page<TaskDevice> page,
                                                         @Param("taskId") Long taskId,
                                                         @Param("algorithmId") Long algorithmId,
                                                         @Param("query") TaskDeviceQueryRequest query);


    int countByTaskIdAndAlgorithmId(@Param("taskId") Long taskId,
                                    @Param("algorithmId") Long algorithmId);


    int deleteByTaskId(@Param("taskId") Long taskId);


    int deleteByTaskIdAlgorithmIdAndDeviceId(@Param("taskId") Long taskId,
                                             @Param("algorithmId") Long algorithmId,
                                             @Param("deviceId") Long deviceId);


    int insertBatch(@Param("list") List<TaskDevice> list);


    int countByDeviceId(@Param("deviceId") Long deviceId);
}
