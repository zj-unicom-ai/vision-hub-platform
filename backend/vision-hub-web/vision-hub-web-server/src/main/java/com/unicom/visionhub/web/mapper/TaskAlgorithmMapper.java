package com.unicom.visionhub.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unicom.visionhub.web.entity.task.TaskAlgorithm;
import com.unicom.visionhub.web.vo.TaskAlgorithmVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface TaskAlgorithmMapper extends BaseMapper<TaskAlgorithm> {


    List<TaskAlgorithmVO> selectListByTaskId(@Param("taskId") Long taskId);


    int insertBatch(@Param("list") List<TaskAlgorithm> list);


    IPage<TaskAlgorithmVO> selectPageByAlgorithmName(
            @Param("page") com.baomidou.mybatisplus.extension.plugins.pagination.Page<TaskAlgorithm> page,
            @Param("algorithmName") String algorithmName);


    int countByAlgorithmId(@Param("algorithmId") Long algorithmId);


    int countRunningByAlgorithmId(@Param("algorithmId") Long algorithmId);
}
