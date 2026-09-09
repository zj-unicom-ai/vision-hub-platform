package com.unicom.visionhub.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unicom.visionhub.web.dto.request.UserCreateRequest;
import com.unicom.visionhub.web.dto.request.UserLoginRequest;
import com.unicom.visionhub.web.dto.request.UserQueryRequest;
import com.unicom.visionhub.web.entity.model.User;
import com.unicom.visionhub.web.vo.CaptchaVO;
import com.unicom.visionhub.web.vo.LoginVO;
import com.unicom.visionhub.web.vo.UserVO;


public interface UserService extends IService<User> {


    LoginVO login(UserLoginRequest loginRequest);


    CaptchaVO generateCaptcha();


    UserVO getByUsername(String username);


    IPage<UserVO> page(UserQueryRequest queryRequest);


    UserVO getDetailById(Long id);


    Long create(UserCreateRequest createRequest);


    Boolean update(Long id, String username, String nickname, String email, String phone, Integer status);


    Boolean delete(Long id);


    Boolean updatePassword(Long id, String oldPassword, String newPassword);
}