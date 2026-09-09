package com.unicom.visionhub.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.visionhub.web.dto.request.EventRecordQueryRequest;
import com.unicom.visionhub.web.entity.event.EventRecord;
import com.unicom.visionhub.web.vo.EventRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface EventRecordMapper extends BaseMapper<EventRecord> {


    IPage<EventRecordVO> selectPageWithVO(Page<EventRecord> page, @Param("query") EventRecordQueryRequest query);


    EventRecordVO selectVOById(@Param("id") Long id);


    List<EventRecordVO> selectListWithVO(@Param("query") EventRecordQueryRequest query);
}
