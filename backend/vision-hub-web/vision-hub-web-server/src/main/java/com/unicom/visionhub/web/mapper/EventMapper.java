package com.unicom.visionhub.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.visionhub.web.dto.request.EventQueryRequest;
import com.unicom.visionhub.web.entity.event.Event;
import com.unicom.visionhub.web.vo.EventVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface EventMapper extends BaseMapper<Event> {


    List<Event> selectListByTaskId(@Param("taskId") Long taskId);


    Event selectByTaskIdAndAlgorithmId(@Param("taskId") Long taskId, @Param("algorithmId") Long algorithmId);


    int deleteByTaskId(@Param("taskId") Long taskId);


    IPage<EventVO> selectPageWithVO(Page<Event> page, @Param("query") EventQueryRequest query);
}
