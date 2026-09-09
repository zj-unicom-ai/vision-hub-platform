package com.unicom.visionhub.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.visionhub.web.entity.model.User;
import com.unicom.visionhub.web.dto.request.UserQueryRequest;
import com.unicom.visionhub.web.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface UserMapper extends BaseMapper<User> {


    IPage<UserVO> selectPageWithVO(@Param("page") Page<User> page,
                                   @Param("query") UserQueryRequest query);


    UserVO selectVOById(@Param("id") Long id);


    UserVO selectVOByUsername(@Param("username") String username);


    int countByUsername(@Param("username") String username,
                         @Param("excludeId") Long excludeId);
}