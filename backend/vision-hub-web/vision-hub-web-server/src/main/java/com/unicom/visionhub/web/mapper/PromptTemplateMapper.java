package com.unicom.visionhub.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.visionhub.web.dto.request.PromptTemplateListRequest;
import com.unicom.visionhub.web.dto.request.PromptTemplateQueryRequest;
import com.unicom.visionhub.web.entity.prompt.PromptTemplate;
import com.unicom.visionhub.web.vo.PromptTemplateVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface PromptTemplateMapper extends BaseMapper<PromptTemplate> {


    IPage<PromptTemplateVO> selectPageWithVO(@Param("page") Page<PromptTemplate> page,
                                              @Param("query") PromptTemplateQueryRequest query);


    PromptTemplateVO selectVOById(@Param("id") Long id);


    int countByTemplateName(@Param("templateName") String templateName,
                            @Param("excludeId") Long excludeId);


    List<PromptTemplateVO> selectListByCondition(@Param("query") PromptTemplateListRequest query);
}
