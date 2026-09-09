package com.unicom.visionhub.web.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unicom.visionhub.common.enums.StatusCode;
import com.unicom.visionhub.common.exception.ResourceExistsException;
import com.unicom.visionhub.common.exception.ResourceNotFoundException;
import com.unicom.visionhub.common.exception.ServiceException;
import com.unicom.visionhub.common.exception.SystemException;
import com.unicom.visionhub.common.util.JwtUtil;
import com.unicom.visionhub.web.entity.model.User;
import com.unicom.visionhub.web.dto.request.UserCreateRequest;
import com.unicom.visionhub.web.dto.request.UserLoginRequest;
import com.unicom.visionhub.web.dto.request.UserQueryRequest;
import com.unicom.visionhub.web.mapper.UserMapper;
import com.unicom.visionhub.web.service.UserService;
import com.unicom.visionhub.web.util.RedisUtil;
import com.unicom.visionhub.web.vo.CaptchaVO;
import com.unicom.visionhub.web.vo.LoginVO;
import com.unicom.visionhub.web.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;


    private static final String CAPTCHA_KEY_PREFIX = "visionhub:captcha:";


    private static final long CAPTCHA_EXPIRE_SECONDS = 300L;

    @Override
    public LoginVO login(UserLoginRequest loginRequest) {
        validateCaptcha(loginRequest.getCaptchaId(), loginRequest.getCaptchaCode());

        UserVO userVO = userMapper.selectVOByUsername(loginRequest.getUsername());
        if (userVO == null) {
            log.warn("【用户】登录失败，用户不存在: {}", loginRequest.getUsername());
            throw new ResourceNotFoundException(StatusCode.USER_NOT_FOUND);
        }

        if (userVO.getStatus() == 0) {
            log.warn("【用户】登录失败，用户已禁用: {}", loginRequest.getUsername());
            throw new ServiceException(StatusCode.USER_DISABLED);
        }

        User user = getById(userVO.getId());
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("【用户】登录失败，密码错误: {}", loginRequest.getUsername());
            throw new ServiceException(StatusCode.USERNAME_PASSWORD_ERROR);
        }

        User updateEntity = new User();
        updateEntity.setId(user.getId());
        updateEntity.setLastLoginAt(LocalDateTime.now());
        updateEntity.setLastLoginIp(getLoginIp());
        updateById(updateEntity);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        String token = JwtUtil.generateToken(String.valueOf(user.getId()), claims);

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setTokenType("Bearer");
        loginVO.setUser(userVO);

        log.info("【用户】登录成功: {}", loginRequest.getUsername());
        return loginVO;
    }

    @Override
    public CaptchaVO generateCaptcha() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 70, 4, 30);
        String captchaId = IdUtil.simpleUUID();
        String code = captcha.getCode().toLowerCase();
        redisUtil.set(CAPTCHA_KEY_PREFIX + captchaId, code, CAPTCHA_EXPIRE_SECONDS);

        CaptchaVO vo = new CaptchaVO();
        vo.setCaptchaId(captchaId);
        vo.setCaptchaImage(captcha.getImageBase64Data());
        vo.setExpireSeconds(CAPTCHA_EXPIRE_SECONDS);
        log.debug("【用户】生成验证码: captchaId={}", captchaId);
        return vo;
    }


    private void validateCaptcha(String captchaId, String captchaCode) {
        String key = CAPTCHA_KEY_PREFIX + captchaId;
        String cached = redisUtil.get(key);
        redisUtil.delete(key);
        if (StrUtil.isBlank(captchaCode) || cached == null || !cached.equalsIgnoreCase(captchaCode)) {
            log.warn("【用户】验证码校验失败: captchaId={}", captchaId);
            throw new ServiceException(StatusCode.CAPTCHA_INVALID);
        }
    }

    @Override
    public UserVO getByUsername(String username) {
        UserVO vo = userMapper.selectVOByUsername(username);
        if (vo != null) {
            enrichVO(vo);
        }
        return vo;
    }

    @Override
    public IPage<UserVO> page(UserQueryRequest queryRequest) {
        Page<User> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        IPage<UserVO> resultPage = userMapper.selectPageWithVO(page, queryRequest);
        resultPage.getRecords().forEach(this::enrichVO);
        return resultPage;
    }

    @Override
    public UserVO getDetailById(Long id) {
        UserVO vo = userMapper.selectVOById(id);
        if (vo == null) {
            log.warn("【用户】查询用户失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.USER_NOT_FOUND);
        }
        enrichVO(vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(UserCreateRequest createRequest) {
        User currentUser = getCurrentUserContext();
        if (currentUser == null || !"unicom".equals(currentUser.getUsername())) {
            log.warn("【用户】创建用户失败，仅管理员有权限，当前用户: {}",
                    currentUser != null ? currentUser.getUsername() : "未登录");
            throw new ServiceException("仅有管理员可创建用户");
        }

        if (userMapper.countByUsername(createRequest.getUsername(), null) > 0) {
            log.warn("【用户】创建用户失败，用户名已存在: {}", createRequest.getUsername());
            throw new ResourceExistsException("用户", "username", createRequest.getUsername());
        }

        User entity = new User();
        entity.setUsername(createRequest.getUsername());
        entity.setPassword(passwordEncoder.encode(createRequest.getPassword()));
        entity.setNickname(createRequest.getNickname());
        entity.setEmail(createRequest.getEmail());
        entity.setPhone(createRequest.getPhone());
        entity.setStatus(1);

        boolean saved = save(entity);
        if (!saved) {
            log.error("【用户】创建用户失败，数据库操作失败");
            throw new SystemException("创建用户失败");
        }

        log.info("【用户】创建用户成功, id={}, username={}", entity.getId(), entity.getUsername());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(Long id, String username, String nickname, String email, String phone, Integer status) {
        User existEntity = getById(id);
        if (existEntity == null) {
            log.warn("【用户】更新用户失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.USER_NOT_FOUND);
        }

        if (StrUtil.isNotBlank(username) && userMapper.countByUsername(username, id) > 0) {
            log.warn("【用户】更新用户失败，用户名已被占用: {}", username);
            throw new ResourceExistsException("用户", "username", username);
        }

        User entity = new User();
        entity.setId(id);
        if (StrUtil.isNotBlank(username)) {
            entity.setUsername(username);
        }
        if (StrUtil.isNotBlank(nickname)) {
            entity.setNickname(nickname);
        }
        if (StrUtil.isNotBlank(email)) {
            entity.setEmail(email);
        }
        if (StrUtil.isNotBlank(phone)) {
            entity.setPhone(phone);
        }
        if (status != null) {
            entity.setStatus(status);
        }

        boolean updated = updateById(entity);
        if (!updated) {
            log.error("【用户】更新用户失败，数据库操作失败");
            throw new SystemException("更新用户失败");
        }

        log.info("【用户】更新用户成功, id={}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        User existEntity = getById(id);
        if (existEntity == null) {
            log.warn("【用户】删除用户失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.USER_NOT_FOUND);
        }

        boolean removed = removeById(id);
        if (!removed) {
            log.error("【用户】删除用户失败，数据库操作失败");
            throw new SystemException("删除用户失败");
        }

        log.info("【用户】删除用户成功, id={}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePassword(Long id, String oldPassword, String newPassword) {
        User existEntity = getById(id);
        if (existEntity == null) {
            log.warn("【用户】修改密码失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(oldPassword, existEntity.getPassword())) {
            log.warn("【用户】修改密码失败，旧密码错误: id={}", id);
            throw new ServiceException(StatusCode.PASSWORD_ERROR);
        }

        if (passwordEncoder.matches(newPassword, existEntity.getPassword())) {
            log.warn("【用户】修改密码失败，新密码不能与旧密码相同: id={}", id);
            throw new ServiceException(StatusCode.PASSWORD_SAME);
        }

        User entity = new User();
        entity.setId(id);
        entity.setPassword(passwordEncoder.encode(newPassword));

        boolean updated = updateById(entity);
        if (!updated) {
            log.error("【用户】修改密码失败，数据库操作失败");
            throw new SystemException("修改密码失败");
        }

        log.info("【用户】修改密码成功, id={}", id);
        return true;
    }


    private void enrichVO(UserVO vo) {
        vo.setStatusDesc(vo.getStatus() != null && vo.getStatus() == 1 ? "启用" : "禁用");
    }


    private String getLoginIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (StrUtil.isBlank(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (StrUtil.isBlank(ip)) {
                    ip = request.getRemoteAddr();
                }
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        } catch (Exception e) {
            log.debug("【用户】获取登录IP失败", e);
        }
        return null;
    }


    private User getCurrentUserContext() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User user) {
                return getById(user.getId());
            }
        } catch (Exception e) {
            log.debug("【用户】获取当前用户上下文失败", e);
        }
        return null;
    }
}