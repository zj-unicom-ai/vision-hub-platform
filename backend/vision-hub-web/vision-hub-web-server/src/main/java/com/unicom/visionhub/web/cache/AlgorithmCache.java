package com.unicom.visionhub.web.cache;

import cn.hutool.core.util.StrUtil;
import com.unicom.visionhub.common.enums.AlgorithmStatus;
import com.unicom.visionhub.common.enums.AlgorithmType;
import com.unicom.visionhub.common.enums.MediaType;
import com.unicom.visionhub.web.mapper.AlgorithmMapper;
import com.unicom.visionhub.web.service.MinioService;
import com.unicom.visionhub.web.util.RedisUtil;
import com.unicom.visionhub.web.vo.AlgorithmVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class AlgorithmCache {

    private static final String ALGORITHM_VO_KEY_PREFIX = "algorithm:vo:";
    private static final long EXPIRE_SECONDS = 1800;

    private final RedisUtil redisUtil;
    private final AlgorithmMapper algorithmMapper;
    private final MinioService minioService;


    public AlgorithmVO getAlgorithmVO(Long algorithmId) {
        if (algorithmId == null) {
            return null;
        }

        String key = ALGORITHM_VO_KEY_PREFIX + algorithmId;

        AlgorithmVO cached = redisUtil.get(key);
        if (cached != null) {
            log.debug("【AlgorithmCache】缓存命中: id={}", algorithmId);
            return cached;
        }

        log.debug("【AlgorithmCache】缓存未命中: id={}", algorithmId);
        AlgorithmVO vo = algorithmMapper.selectVOById(algorithmId);
        if (vo == null) {
            return null;
        }

        enrichVO(vo);
        redisUtil.set(key, vo, EXPIRE_SECONDS, TimeUnit.SECONDS);

        return vo;
    }


    public void evictAlgorithm(Long algorithmId) {
        if (algorithmId == null) {
            return;
        }
        redisUtil.delete(ALGORITHM_VO_KEY_PREFIX + algorithmId);
        log.debug("【AlgorithmCache】移除算法缓存: id={}", algorithmId);
    }


    private void enrichVO(AlgorithmVO vo) {
        AlgorithmType algorithmType = AlgorithmType.fromCode(vo.getAlgorithmType());
        vo.setAlgorithmTypeDesc(algorithmType != null ? algorithmType.getDescription() : "未知");

        MediaType mediaType = MediaType.fromCode(vo.getMediaType());
        vo.setMediaTypeDesc(mediaType != null ? mediaType.getDescription() : "未知");

        AlgorithmStatus status = AlgorithmStatus.fromValue(vo.getStatus());
        vo.setStatusDesc(status != null ? status.getDescription() : "未知");

        if (StrUtil.isNotBlank(vo.getIcon())) {
            vo.setIconUrl(minioService.getFileUrlWithCdn(vo.getIcon()));
        }
    }
}