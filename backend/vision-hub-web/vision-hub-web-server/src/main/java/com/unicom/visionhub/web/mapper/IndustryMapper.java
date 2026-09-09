package com.unicom.visionhub.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.visionhub.web.dto.request.IndustryListRequest;
import com.unicom.visionhub.web.dto.request.IndustryQueryRequest;
import com.unicom.visionhub.web.entity.industry.Industry;
import com.unicom.visionhub.web.vo.IndustryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface IndustryMapper extends BaseMapper<Industry> {


    IPage<IndustryVO> selectPageWithVO(Page<Industry> page, @Param("query") IndustryQueryRequest queryRequest);


    IndustryVO selectVOById(@Param("id") Long id);


    int countByIndustryCode(@Param("industryCode") String industryCode, @Param("excludeId") Long excludeId);


    List<IndustryVO> selectListWithVO(@Param("query") IndustryListRequest queryRequest);
}
