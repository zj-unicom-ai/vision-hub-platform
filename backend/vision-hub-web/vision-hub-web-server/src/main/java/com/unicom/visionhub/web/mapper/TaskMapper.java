package com.unicom.visionhub.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.visionhub.web.dto.request.TaskQueryRequest;
import com.unicom.visionhub.web.entity.task.Task;
import com.unicom.visionhub.web.vo.TaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface TaskMapper extends BaseMapper<Task> {


    IPage<TaskVO> selectPageWithVO(Page<Task> page, @Param("query") TaskQueryRequest queryRequest);


    TaskVO selectVOById(@Param("id") Long id);


    int countByTaskName(@Param("taskName") String taskName, @Param("excludeId") Long excludeId);
}
