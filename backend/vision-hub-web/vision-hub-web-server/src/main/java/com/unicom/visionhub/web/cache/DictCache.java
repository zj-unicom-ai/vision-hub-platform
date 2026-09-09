package com.unicom.visionhub.web.cache;

import com.unicom.visionhub.common.enums.AlgorithmType;
import com.unicom.visionhub.common.enums.ModelType;
import com.unicom.visionhub.common.enums.ModelVersion;
import com.unicom.visionhub.common.enums.Provider;
import com.unicom.visionhub.common.exception.ServiceException;
import com.unicom.visionhub.web.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class DictCache {

    private static final String DICT_KEY_PREFIX = "dict:";
    private static final long EXPIRE_SECONDS = 3600;

    private final RedisUtil redisUtil;


    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> queryDict(String type, Map<String, String> params) {
        String key = buildKey(type, params);

        List<Map<String, Object>> cached = redisUtil.get(key);
        if (cached != null) {
            log.debug("【DictCache】缓存命中: type={}, params={}", type, params);
            return cached;
        }

        log.debug("【DictCache】缓存未命中: type={}, params={}", type, params);
        List<Map<String, Object>> result = queryDictFromSource(type, params);

        if (result != null) {
            redisUtil.set(key, result, EXPIRE_SECONDS, TimeUnit.SECONDS);
        }

        return result;
    }


    private List<Map<String, Object>> queryDictFromSource(String type, Map<String, String> params) {
        return switch (type) {
            case "provider" -> getProviders(params.get("keyword"));
            case "model-type" -> getModelTypes(params.get("keyword"));
            case "model-version" -> getModelVersions(params);
            case "algorithm-type" -> getAlgorithmTypes();
            default -> throw new ServiceException("不支持的字典类型: " + type);
        };
    }


    private List<Map<String, Object>> getProviders(String keyword) {
        return Provider.search(keyword).stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", p.getCode());
                    map.put("description", p.getDescription());
                    return map;
                })
                .collect(Collectors.toList());
    }


    private List<Map<String, Object>> getModelTypes(String keyword) {
        return ModelType.search(keyword).stream()
                .map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", t.getCode());
                    map.put("description", t.getDescription());
                    return map;
                })
                .collect(Collectors.toList());
    }


    private List<Map<String, Object>> getModelVersions(Map<String, String> params) {
        String provider = params.get("provider");
        String modelType = params.get("modelType");

        if (provider == null || modelType == null) {
            throw new ServiceException("查询 model-version 时必须指定 provider 和 modelType 参数");
        }
        return ModelVersion.getVersions(provider, modelType).stream()
                .map(v -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("description", v.get("description"));
                    map.put("isHighVersion", v.get("isHighVersion"));
                    return map;
                })
                .collect(Collectors.toList());
    }


    private List<Map<String, Object>> getAlgorithmTypes() {
        return Arrays.stream(AlgorithmType.values())
                .map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", t.getCode());
                    map.put("description", t.getDescription());
                    return map;
                })
                .collect(Collectors.toList());
    }


    public void evictByType(String type) {
        String pattern = DICT_KEY_PREFIX + type + "*";
        Iterable<String> keys = redisUtil.keys(pattern);
        if (keys != null) {
            for (String key : keys) {
                redisUtil.delete(key);
            }
        }
        log.debug("【DictCache】清除字典缓存: type={}", type);
    }


    public void evictAll() {
        Iterable<String> keys = redisUtil.keys(DICT_KEY_PREFIX + "*");
        if (keys != null) {
            for (String key : keys) {
                redisUtil.delete(key);
            }
        }
        log.debug("【DictCache】清除所有字典缓存");
    }


    private String buildKey(String type, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(DICT_KEY_PREFIX).append(type);
        if (params != null && !params.isEmpty()) {
            sb.append(":").append(params.hashCode());
        }
        return sb.toString();
    }
}