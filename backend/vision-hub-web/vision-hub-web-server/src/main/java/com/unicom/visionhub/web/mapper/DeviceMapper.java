package com.unicom.visionhub.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.visionhub.web.dto.request.DeviceQueryRequest;
import com.unicom.visionhub.web.entity.device.Device;
import com.unicom.visionhub.web.vo.DeviceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface DeviceMapper extends BaseMapper<Device> {


    IPage<DeviceVO> selectPageWithVO(Page<Device> page, @Param("query") DeviceQueryRequest queryRequest);


    DeviceVO selectVOById(@Param("id") Long id);


    int countByDeviceCode(@Param("deviceCode") String deviceCode, @Param("excludeId") Long excludeId);


    List<Device> selectEnabledStreamDevices();
}
