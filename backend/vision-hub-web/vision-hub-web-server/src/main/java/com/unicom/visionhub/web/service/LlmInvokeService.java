package com.unicom.visionhub.web.service;

import com.unicom.visionhub.web.entity.model.LlmModel;

import java.util.Map;


public interface LlmInvokeService {


    Map<String, Object> invoke(LlmModel llmModel, String prompt, Integer maxTokens);


    Map<String, Object> invoke(LlmModel llmModel, String prompt);


    Map<String, Object> invoke(LlmModel llmModel, String prompt, String imageUrl, Integer maxTokens);


    Map<String, Object> invoke(LlmModel llmModel, String prompt, String imageUrl);
}
