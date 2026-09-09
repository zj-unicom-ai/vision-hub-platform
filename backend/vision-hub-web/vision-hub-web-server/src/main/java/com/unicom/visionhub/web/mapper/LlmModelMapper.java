package com.unicom.visionhub.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.visionhub.web.dto.request.LlmModelQueryRequest;
import com.unicom.visionhub.web.entity.model.LlmModel;
import com.unicom.visionhub.web.vo.LlmModelVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface LlmModelMapper extends BaseMapper<LlmModel> {


    IPage<LlmModelVO> selectPageWithVO(@Param("page") Page<LlmModel> page,
                                        @Param("query") LlmModelQueryRequest query);


    LlmModelVO selectVOById(@Param("id") Long id);


    int countByModelName(@Param("modelName") String modelName,
                         @Param("excludeId") Long excludeId);
}
