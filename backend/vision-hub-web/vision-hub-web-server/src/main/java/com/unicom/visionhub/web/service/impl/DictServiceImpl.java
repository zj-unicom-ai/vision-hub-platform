package com.unicom.visionhub.web.service.impl;

import com.unicom.visionhub.web.cache.DictCache;
import com.unicom.visionhub.web.service.DictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final DictCache dictCache;

    @Override
    public List<Map<String, Object>> queryDict(String type, Map<String, String> params) {
        return dictCache.queryDict(type, params);
    }
}