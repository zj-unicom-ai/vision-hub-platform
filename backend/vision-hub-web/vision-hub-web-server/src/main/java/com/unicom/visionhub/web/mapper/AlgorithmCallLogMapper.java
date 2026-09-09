package com.unicom.visionhub.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicom.visionhub.web.entity.algorithm.AlgorithmCallLog;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface AlgorithmCallLogMapper extends BaseMapper<AlgorithmCallLog> {


    int deleteOldRecords(int days);
}
