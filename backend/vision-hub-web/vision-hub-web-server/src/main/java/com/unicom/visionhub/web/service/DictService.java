package com.unicom.visionhub.web.service;

import java.util.List;
import java.util.Map;


public interface DictService {


    List<Map<String, Object>> queryDict(String type, Map<String, String> params);
}
