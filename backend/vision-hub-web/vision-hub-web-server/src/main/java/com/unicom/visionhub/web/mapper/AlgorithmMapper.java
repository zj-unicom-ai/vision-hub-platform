package com.unicom.visionhub.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.visionhub.web.dto.request.AlgorithmQueryRequest;
import com.unicom.visionhub.web.entity.algorithm.Algorithm;
import com.unicom.visionhub.web.vo.AlgorithmVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface AlgorithmMapper extends BaseMapper<Algorithm> {


    IPage<AlgorithmVO> selectPageWithVO(@Param("page") Page<Algorithm> page,
                                        @Param("query") AlgorithmQueryRequest query);


    AlgorithmVO selectVOById(@Param("id") Long id);


    Algorithm selectByAlgorithmCode(@Param("algorithmCode") String algorithmCode);


    int countByAlgorithmCode(@Param("algorithmCode") String algorithmCode,
                             @Param("excludeId") Long excludeId);


    int countByLlmModelId(@Param("llmModelId") Long llmModelId);
}
