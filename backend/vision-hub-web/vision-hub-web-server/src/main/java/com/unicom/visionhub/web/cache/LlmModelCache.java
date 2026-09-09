package com.unicom.visionhub.web.cache;

import cn.hutool.core.util.StrUtil;
import com.unicom.visionhub.common.enums.ModelType;
import com.unicom.visionhub.common.enums.Provider;
import com.unicom.visionhub.web.mapper.LlmModelMapper;
import com.unicom.visionhub.web.service.MinioService;
import com.unicom.visionhub.web.util.RedisUtil;
import com.unicom.visionhub.web.vo.LlmModelVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class LlmModelCache {

    private static final String LLM_MODEL_VO_KEY_PREFIX = "llm_model:vo:";
    private static final long EXPIRE_SECONDS = 1800;

    private final RedisUtil redisUtil;
    private final LlmModelMapper llmModelMapper;
    private final MinioService minioService;


    public LlmModelVO getLlmModelVO(Long modelId) {
        if (modelId == null) {
            return null;
        }

        String key = LLM_MODEL_VO_KEY_PREFIX + modelId;

        LlmModelVO cached = redisUtil.get(key);
        if (cached != null) {
            log.debug("【LlmModelCache】缓存命中: id={}", modelId);
            return cached;
        }

        log.debug("【LlmModelCache】缓存未命中: id={}", modelId);
        LlmModelVO vo = llmModelMapper.selectVOById(modelId);
        if (vo == null) {
            return null;
        }

        enrichVO(vo);
        redisUtil.set(key, vo, EXPIRE_SECONDS, TimeUnit.SECONDS);

        return vo;
    }


    public void evictLlmModel(Long modelId) {
        if (modelId == null) {
            return;
        }
        redisUtil.delete(LLM_MODEL_VO_KEY_PREFIX + modelId);
        log.debug("【LlmModelCache】移除大模型缓存: id={}", modelId);
    }


    private void enrichVO(LlmModelVO vo) {
        ModelType modelType = ModelType.fromCode(vo.getModelType());
        vo.setModelTypeDesc(modelType != null ? modelType.getDescription() : "其他");

        Provider provider = Provider.fromCode(vo.getProvider());
        vo.setProviderDesc(provider != null ? provider.getDescription() : "其他");

        vo.setStatusDesc(vo.getStatus() == 1 ? "启用" : "禁用");

        if (StrUtil.isNotBlank(vo.getIcon())) {
            vo.setIconUrl(minioService.getFileUrlWithCdn(vo.getIcon()));
        }

        maskApiKey(vo);
    }


    private void maskApiKey(LlmModelVO vo) {
        if (StrUtil.isNotBlank(vo.getApiKey())) {
            String apiKey = vo.getApiKey();
            if (apiKey.length() > 10) {
                vo.setApiKey(apiKey.substring(0, 8) + "****" + apiKey.substring(apiKey.length() - 4));
            } else {
                vo.setApiKey("****");
            }
        }
    }
}