package com.unicom.visionhub.web.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicom.visionhub.common.enums.AlgorithmExecStatus;
import com.unicom.visionhub.common.enums.AlgorithmRecognitionCode;
import com.unicom.visionhub.common.enums.AlgorithmStatus;
import com.unicom.visionhub.common.enums.AlgorithmType;
import com.unicom.visionhub.common.enums.MediaType;
import com.unicom.visionhub.common.enums.StatusCode;
import com.unicom.visionhub.common.exception.ResourceExistsException;
import com.unicom.visionhub.common.exception.ResourceNotFoundException;
import com.unicom.visionhub.common.exception.ServiceException;
import com.unicom.visionhub.common.exception.SystemException;
import com.unicom.visionhub.common.util.JsonUtil;
import com.unicom.visionhub.web.cache.AlgorithmCache;
import com.unicom.visionhub.web.dto.OutputFormatField;
import com.unicom.visionhub.web.dto.request.AlgorithmCreateRequest;
import com.unicom.visionhub.web.dto.request.AlgorithmExecuteRequest;
import com.unicom.visionhub.web.dto.request.AlgorithmQueryRequest;
import com.unicom.visionhub.web.dto.request.AlgorithmUpdateRequest;
import com.unicom.visionhub.web.dto.response.AlgorithmExecuteResponse;
import com.unicom.visionhub.web.entity.algorithm.Algorithm;
import com.unicom.visionhub.web.entity.algorithm.AlgorithmCallLog;
import com.unicom.visionhub.web.entity.model.LlmModel;
import com.unicom.visionhub.web.mapper.AlgorithmCallLogMapper;
import com.unicom.visionhub.web.mapper.AlgorithmMapper;
import com.unicom.visionhub.web.mapper.LlmModelMapper;
import com.unicom.visionhub.web.mapper.TaskAlgorithmMapper;
import com.unicom.visionhub.web.service.AlgorithmService;
import com.unicom.visionhub.web.service.LlmInvokeService;
import com.unicom.visionhub.web.service.MinioService;
import com.unicom.visionhub.web.vo.AlgorithmVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class AlgorithmServiceImpl extends ServiceImpl<AlgorithmMapper, Algorithm> implements AlgorithmService {

    private final AlgorithmMapper algorithmMapper;
    private final AlgorithmCallLogMapper callLogMapper;
    private final LlmModelMapper llmModelMapper;
    private final TaskAlgorithmMapper taskAlgorithmMapper;
    private final LlmInvokeService llmInvokeService;
    private final MinioService minioService;
    private final AlgorithmCache algorithmCache;
    private final ObjectMapper objectMapper;

    @Override
    public IPage<AlgorithmVO> page(AlgorithmQueryRequest queryRequest) {
        Page<Algorithm> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        IPage<AlgorithmVO> resultPage = algorithmMapper.selectPageWithVO(page, queryRequest);
        resultPage.getRecords().forEach(this::enrichVO);
        return resultPage;
    }

    @Override
    public AlgorithmVO getDetailById(Long id) {
        AlgorithmVO vo = algorithmCache.getAlgorithmVO(id);
        if (vo == null) {
            log.warn("【算法】查询算法失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.ALGORITHM_NOT_FOUND);
        }
        return vo;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AlgorithmCreateRequest createRequest) {
        if (algorithmMapper.countByAlgorithmCode(createRequest.getAlgorithmCode(), null) > 0) {
            log.warn("【算法】创建算法失败，算法编码已存在: {}", createRequest.getAlgorithmCode());
            throw new ResourceExistsException("算法", "algorithm_code", createRequest.getAlgorithmCode());
        }

        if (StrUtil.isBlank(createRequest.getAlgorithmType())
                || AlgorithmType.fromCode(createRequest.getAlgorithmType()) == null) {
            log.warn("【算法】创建算法失败，算法类型无效: {}", createRequest.getAlgorithmType());
            throw new ServiceException("算法类型无效，仅支持: llm(大模型类算法), vision(视觉模型算法)");
        }

        if (StrUtil.isBlank(createRequest.getMediaType())
                || MediaType.fromCode(createRequest.getMediaType()) == null) {
            log.warn("【算法】创建算法失败，介质类型无效: {}", createRequest.getMediaType());
            throw new ServiceException("介质类型无效，仅支持: image(图片), stream(视频流)");
        }

        if (AlgorithmType.LLM.getCode().equals(createRequest.getAlgorithmType())
                && CollectionUtils.isEmpty(createRequest.getOutputFormat())) {
            createRequest.setOutputFormat(getDefaultOutputFormat());
            log.info("【算法】创建算法使用默认输出格式, algorithmCode={}", createRequest.getAlgorithmCode());
        }

        validateAlgorithmConfig(createRequest.getAlgorithmType(), createRequest);

        Algorithm entity = BeanUtil.copyProperties(createRequest, Algorithm.class);
        entity.setSortOrder(createRequest.getSortOrder() != null ? createRequest.getSortOrder() : 0);
        entity.setVersion(StrUtil.isNotBlank(createRequest.getVersion()) ? createRequest.getVersion() : "1.0.0");
        entity.setStatus(AlgorithmStatus.UNPUBLISHED.getValue());

        boolean saved = save(entity);
        if (!saved) {
            log.error("【算法】创建算法失败，数据库操作失败");
            throw new SystemException("创建算法失败");
        }

        log.info("【算法】创建算法成功, id={}, code={}", entity.getId(), entity.getAlgorithmCode());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(AlgorithmUpdateRequest updateRequest) {
        Algorithm existEntity = getById(updateRequest.getId());
        if (existEntity == null) {
            log.warn("【算法】更新算法失败，id={} 不存在", updateRequest.getId());
            throw new ResourceNotFoundException(StatusCode.ALGORITHM_NOT_FOUND);
        }

        if (StrUtil.isNotBlank(updateRequest.getAlgorithmCode()) &&
                algorithmMapper.countByAlgorithmCode(updateRequest.getAlgorithmCode(), updateRequest.getId()) > 0) {
            log.warn("【算法】更新算法失败，算法编码已被占用: {}", updateRequest.getAlgorithmCode());
            throw new ResourceExistsException("算法", "algorithm_code", updateRequest.getAlgorithmCode());
        }

        if (StrUtil.isNotBlank(updateRequest.getAlgorithmType())
                && AlgorithmType.fromCode(updateRequest.getAlgorithmType()) == null) {
            log.warn("【算法】更新算法失败，算法类型无效: {}", updateRequest.getAlgorithmType());
            throw new ServiceException("算法类型无效，仅支持: llm(大模型类算法), vision(视觉模型算法)");
        }

        if (StrUtil.isNotBlank(updateRequest.getMediaType())
                && MediaType.fromCode(updateRequest.getMediaType()) == null) {
            log.warn("【算法】更新算法失败，介质类型无效: {}", updateRequest.getMediaType());
            throw new ServiceException("介质类型无效，仅支持: image(图片), stream(视频流)");
        }

        String algorithmType = updateRequest.getAlgorithmType();
        if (StrUtil.isBlank(algorithmType)) {
            algorithmType = existEntity.getAlgorithmType();
        }

        if (AlgorithmType.LLM.getCode().equals(algorithmType)
                && CollectionUtils.isEmpty(updateRequest.getOutputFormat())) {
            updateRequest.setOutputFormat(getDefaultOutputFormat());
            log.info("【算法】更新算法使用默认输出格式, id={}", updateRequest.getId());
        }

        Algorithm entity = BeanUtil.copyProperties(updateRequest, Algorithm.class);

        boolean updated = updateById(entity);
        if (!updated) {
            log.error("【算法】更新算法失败，数据库操作失败");
            throw new SystemException("更新算法失败");
        }

        log.info("【算法】更新算法成功, id={}", updateRequest.getId());

        algorithmCache.evictAlgorithm(updateRequest.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        Algorithm existEntity = getById(id);
        if (existEntity == null) {
            log.warn("【算法】删除算法失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.ALGORITHM_NOT_FOUND);
        }

        int taskCount = taskAlgorithmMapper.countByAlgorithmId(id);
        if (taskCount > 0) {
            log.warn("【算法】删除算法失败，被 {} 个任务引用", taskCount);
            throw new ServiceException(StatusCode.ALGORITHM_IN_USE_BY_TASK.getMessage());
        }

        boolean removed = removeById(id);
        if (!removed) {
            log.error("【算法】删除算法失败，数据库操作失败");
            throw new SystemException("删除算法失败");
        }

        log.info("【算法】删除算法成功, id={}", id);

        algorithmCache.evictAlgorithm(id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long id, Integer status) {
        Algorithm existEntity = getById(id);
        if (existEntity == null) {
            log.warn("【算法】更新算法状态失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.ALGORITHM_NOT_FOUND);
        }

        if (status != null && AlgorithmStatus.UNPUBLISHED.getValue().equals(status)) {
            int runningTaskCount = taskAlgorithmMapper.countRunningByAlgorithmId(id);
            if (runningTaskCount > 0) {
                log.warn("【算法】停用算法失败，有 {} 个运行中的任务引用该算法", runningTaskCount);
                throw new ServiceException(StatusCode.ALGORITHM_DISABLED_IN_USE.getMessage());
            }
        }

        Algorithm entity = new Algorithm();
        entity.setId(id);
        entity.setStatus(status);

        boolean updated = updateById(entity);
        if (!updated) {
            log.error("【算法】更新算法状态失败，数据库操作失败");
            throw new SystemException("更新状态失败");
        }

        log.info("【算法】更新算法状态成功, id={}, status={}", id, status);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlgorithmExecuteResponse execute(AlgorithmExecuteRequest executeRequest) {
        return doExecute(executeRequest, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlgorithmExecuteResponse executeForTask(AlgorithmExecuteRequest executeRequest, Long taskId, Long deviceId) {
        return doExecute(executeRequest, taskId, deviceId);
    }

    private AlgorithmExecuteResponse doExecute(AlgorithmExecuteRequest executeRequest, Long taskId, Long deviceId) {
        LocalDateTime startTime = LocalDateTime.now();
        String requestId = StrUtil.isNotBlank(executeRequest.getRequestId()) ?
                executeRequest.getRequestId() : IdUtil.simpleUUID();

        Algorithm algorithm = algorithmMapper.selectByAlgorithmCode(executeRequest.getAlgorithmCode());
        if (algorithm == null) {
            log.warn("【算法】执行算法失败，算法编码不存在: {}", executeRequest.getAlgorithmCode());
            throw new ResourceNotFoundException(StatusCode.ALGORITHM_NOT_FOUND);
        }

        AlgorithmExecuteResponse.AlgorithmExecuteResponseBuilder responseBuilder = AlgorithmExecuteResponse.builder()
                .requestId(requestId)
                .algorithmCode(algorithm.getAlgorithmCode())
                .algorithmName(algorithm.getAlgorithmName())
                .startTime(startTime);

        AlgorithmCallLog callLog = null;
        if (taskId != null) {
            callLog = new AlgorithmCallLog();
            callLog.setTaskId(taskId);
            callLog.setDeviceId(deviceId);
            callLog.setAlgorithmId(algorithm.getId());
            callLog.setAlgorithmCode(algorithm.getAlgorithmCode());
            callLog.setAlgorithmType(algorithm.getAlgorithmType());
            callLog.setRequestId(requestId);
            callLog.setStartedAt(startTime);
        }

        String originalImageUrl = executeRequest.getInputImageUrl();

        try {
            Map<String, Object> outputData;
            if (AlgorithmType.LLM.getCode().equals(algorithm.getAlgorithmType())) {
                outputData = executeLLMAlgorithm(algorithm, executeRequest.getInputImageUrl());
            } else if (AlgorithmType.VISION.getCode().equals(algorithm.getAlgorithmType())) {
                outputData = executeVisionAlgorithm(algorithm);
            } else {
                throw new ServiceException("不支持的算法类型: " + algorithm.getAlgorithmType());
            }

            Map<String, Object> outputDataBefore = extractResultForLog(outputData);
            Integer isAlarm = extractIsAlarm(outputData);

            String resultImageUrl = originalImageUrl;
            String resultImage = executeRequest.getInputImage();
            if (StrUtil.isNotBlank(originalImageUrl)) {
                String drawnObjectName = drawBoundingBoxesIfNeeded(outputData, originalImageUrl, algorithm.getAlgorithmCode(), requestId);
                if (StrUtil.isNotBlank(drawnObjectName)) {
                    resultImage = drawnObjectName;
                    resultImageUrl = minioService.getFileUrlWithCdn(drawnObjectName);
                }
            }

            LocalDateTime endTime = LocalDateTime.now();
            long duration = java.time.Duration.between(startTime, endTime).toMillis();

            responseBuilder.status(AlgorithmExecStatus.SUCCESS.getCode())
                    .executeDuration(duration)
                    .outputData(outputData)
                    .resultImageUrl(resultImageUrl)
                    .resultImage(resultImage)
                    .endTime(endTime);

            if (callLog != null) {
                callLog.setExecStatus(AlgorithmExecStatus.SUCCESS.getCode());
                callLog.setExecuteSource(taskId != null ? "task" : "test");
                callLog.setOutputData((Map<String, Object>) outputData.get("result"));
                callLog.setOutputDataBefore(outputDataBefore);
                callLog.setIsAlarmBefore(isAlarm);
                callLog.setIsAlarmAfter(isAlarm);
                callLog.setRawResponse((String) outputData.get("rawResponse"));
                callLog.setResultImage(resultImage);
                callLog.setPromptTokens((Integer) outputData.get("promptTokens"));
                callLog.setCompletionTokens((Integer) outputData.get("completionTokens"));
                callLog.setTotalTokens((Integer) outputData.get("totalTokens"));

                Map<String, Object> result = (Map<String, Object>) outputData.get("result");
                if (result != null) {
                    callLog.setRecognizeCode((Integer) result.get("recognizeCode"));
                    callLog.setRecognizeMessage((String) result.get("recognizeMessage"));
                }
                callLog.setExecuteDuration(duration);
                callLog.setFinishedAt(endTime);
            }

        } catch (Exception e) {
            LocalDateTime endTime = LocalDateTime.now();
            long duration = java.time.Duration.between(startTime, endTime).toMillis();

            log.error("【算法】算法执行失败: {}", e.getMessage(), e);
            responseBuilder.status(AlgorithmExecStatus.FAILED.getCode())
                    .executeDuration(duration)
                    .errorMessage(e.getMessage())
                    .resultImageUrl(originalImageUrl)
                    .endTime(endTime);

            if (callLog != null) {
                callLog.setExecStatus(AlgorithmExecStatus.FAILED.getCode());
                callLog.setErrorMessage(e.getMessage());
                callLog.setExecuteDuration(duration);
                callLog.setFinishedAt(endTime);
            }
        } finally {
            if (callLog != null) {
                callLogMapper.insert(callLog);
            }
        }

        return responseBuilder.build();
    }

    private Map<String, Object> executeLLMAlgorithm(Algorithm algorithm, String imageUrl) {
        if (algorithm.getLlmModelId() == null) {
            throw new ServiceException("大模型类算法未配置大模型");
        }
        if (StrUtil.isBlank(algorithm.getDetectionContent())) {
            throw new ServiceException("大模型类算法未配置检测内容");
        }

        LlmModel llmModel = llmModelMapper.selectById(algorithm.getLlmModelId());
        if (llmModel == null) {
            log.error("【算法】大模型配置不存在, modelId={}", algorithm.getLlmModelId());
            throw new ServiceException("大模型配置不存在");
        }
        if (llmModel.getStatus() == 0) {
            log.warn("【算法】大模型已禁用, modelId={}", algorithm.getLlmModelId());
            throw new ServiceException("大模型已禁用");
        }

        StringBuilder fullPrompt = new StringBuilder();
        fullPrompt.append(algorithm.getDetectionContent());

        if (algorithm.getOutputFormat() != null && !algorithm.getOutputFormat().isEmpty()) {
            fullPrompt.append("\n\n请严格按照以下JSON格式返回结果，不要包含任何其他文字，不要添加markdown代码块标记：\n");
            String jsonFormat = buildOutputJsonFormat(algorithm.getOutputFormat());
            fullPrompt.append(jsonFormat);
        }

        log.info("【算法】执行大模型算法: algorithmCode={}, modelId={}, model={}, hasImage={}",
                algorithm.getAlgorithmCode(), algorithm.getLlmModelId(), llmModel.getModelId(), StrUtil.isNotBlank(imageUrl));
        log.debug("【算法】完整提示词: {}", fullPrompt);
        log.debug("【算法】图片URL: {}", imageUrl);

        Map<String, Object> invokeResult;
        if (StrUtil.isNotBlank(imageUrl)) {
            invokeResult = llmInvokeService.invoke(llmModel, fullPrompt.toString(), imageUrl);
        } else {
            invokeResult = llmInvokeService.invoke(llmModel, fullPrompt.toString());
        }
        String rawResponse = (String) invokeResult.get("response");

        Map<String, Object> parseResult = parseOutputFormat(rawResponse, algorithm.getOutputFormat());

        Map<String, Object> outputData = new HashMap<>();
        outputData.put("success", true);
        outputData.put("model", llmModel.getModelName());
        outputData.put("modelId", llmModel.getModelId());
        outputData.put("provider", invokeResult.get("provider"));
        outputData.put("rawResponse", rawResponse);
        outputData.put("result", parseResult);
        outputData.put("duration", invokeResult.get("duration"));
        outputData.put("promptTokens", invokeResult.get("promptTokens"));
        outputData.put("completionTokens", invokeResult.get("completionTokens"));
        outputData.put("totalTokens", invokeResult.get("totalTokens"));

        return outputData;
    }


    private String buildOutputJsonFormat(Map<String, OutputFormatField> outputFormat) {
        StringBuilder sb = new StringBuilder("{\n");
        boolean first = true;
        for (Map.Entry<String, OutputFormatField> entry : outputFormat.entrySet()) {
            if (!first) {
                sb.append(",\n");
            }
            first = false;
            String key = entry.getKey();
            OutputFormatField field = entry.getValue();
            sb.append("  \"").append(key).append("\": ");

            String placeholder = getPlaceholder(field);
            sb.append(placeholder);

            if (StrUtil.isNotBlank(field.getDesc())) {
                sb.append("  // ").append(field.getDesc());
            }
            if (Boolean.TRUE.equals(field.getRequired())) {
                sb.append(" [必填]");
            }
        }
        sb.append("\n}");
        return sb.toString();
    }


    private String getPlaceholder(OutputFormatField field) {
        if (field.getExampleValue() != null) {
            return JsonUtil.toJson(field.getExampleValue());
        }

        return switch (field.getType()) {
            case "string" -> "\"\"";
            case "int" -> "0";
            case "double" -> "0.0";
            case "bool" -> "false";
            case "enum" -> {
                if (field.getEnumValues() != null && !field.getEnumValues().isEmpty()) {
                    Object firstValue = field.getEnumValues().getFirst();
                    yield firstValue instanceof Number ? firstValue.toString() : "\"" + firstValue + "\"";
                }
                yield "\"枚举值\"";
            }
            case "array" -> buildArrayPlaceholder(field);
            case "object" -> buildObjectPlaceholder(field);
            default -> "\"\"";
        };
    }


    private String buildArrayPlaceholder(OutputFormatField field) {
        if (field.getItems() == null) {
            return "[]";
        }
        String itemPlaceholder = getPlaceholder(field.getItems());
        return "[" + itemPlaceholder + "]";
    }


    private String buildObjectPlaceholder(OutputFormatField field) {
        if (field.getProperties() == null || field.getProperties().isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{\n");
        boolean first = true;
        for (Map.Entry<String, OutputFormatField> entry : field.getProperties().entrySet()) {
            if (!first) {
                sb.append(",\n");
            }
            first = false;
            sb.append("    \"").append(entry.getKey()).append("\": ").append(getPlaceholder(entry.getValue()));
        }
        sb.append("\n  }");
        return sb.toString();
    }


    private Map<String, Object> parseOutputFormat(String rawResponse, Map<String, OutputFormatField> outputFormat) {
        Map<String, Object> result = new HashMap<>();

        if (outputFormat == null || outputFormat.isEmpty()) {
            result.put("recognizeCode", AlgorithmRecognitionCode.SUCCESS.getCode());
            result.put("recognizeMessage", AlgorithmRecognitionCode.SUCCESS.getMessage());
            result.put("data", Map.of("rawResult", rawResponse));
            result.put("text", rawResponse);
            return result;
        }

        if (rawResponse == null || rawResponse.isEmpty()) {
            Map<String, Object> defaultOutput = createDefaultOutput(outputFormat);
            result.put("recognizeCode", AlgorithmRecognitionCode.LLM_EMPTY_RESPONSE.getCode());
            result.put("recognizeMessage", AlgorithmRecognitionCode.LLM_EMPTY_RESPONSE.getMessage());
            result.put("data", defaultOutput);
            result.put("text", formatOutputAsText(defaultOutput, outputFormat));
            return result;
        }

        try {
            String cleanedResponse = cleanMarkdownCodeBlock(rawResponse);
            JsonNode jsonNode = JsonUtil.parse(cleanedResponse);
            Map<String, Object> data = parseJsonNodeByFormat(jsonNode, outputFormat);
            int missingFields = countMissingRequiredFields(data, outputFormat);

            if (missingFields > 0) {
                result.put("recognizeCode", AlgorithmRecognitionCode.LLM_MISSING_FIELDS.getCode());
                result.put("recognizeMessage", AlgorithmRecognitionCode.LLM_MISSING_FIELDS.getMessage());
            } else {
                result.put("recognizeCode", AlgorithmRecognitionCode.SUCCESS.getCode());
                result.put("recognizeMessage", AlgorithmRecognitionCode.SUCCESS.getMessage());
            }
            result.put("data", data);
            result.put("text", formatOutputAsText(data, outputFormat));
            return result;
        } catch (Exception e) {
            Map<String, Object> defaultOutput = createDefaultOutput(outputFormat);
            if (e.getCause() instanceof com.fasterxml.jackson.core.JsonProcessingException
                    || e instanceof com.fasterxml.jackson.core.JsonProcessingException) {
                log.warn("【算法】响应不是有效JSON: {}", rawResponse);
                result.put("recognizeCode", AlgorithmRecognitionCode.LLM_JSON_NOT_FOUND.getCode());
                result.put("recognizeMessage", AlgorithmRecognitionCode.LLM_JSON_NOT_FOUND.getMessage());
            } else {
                log.error("【算法】解析输出格式失败: {}", rawResponse, e);
                result.put("recognizeCode", AlgorithmRecognitionCode.LLM_PARSE_FAILED.getCode());
                result.put("recognizeMessage", AlgorithmRecognitionCode.LLM_PARSE_FAILED.getMessage() + ": " + e.getMessage());
            }
            result.put("data", defaultOutput);
            result.put("text", formatOutputAsText(defaultOutput, outputFormat));
            return result;
        }
    }


    private String cleanMarkdownCodeBlock(String text) {
        if (text == null) {
            return null;
        }
        String cleaned = text.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim();
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }
        return cleaned;
    }


    private Map<String, Object> parseJsonNodeByFormat(JsonNode jsonNode, Map<String, OutputFormatField> outputFormat) {
        Map<String, Object> data = new HashMap<>();
        if (jsonNode == null || !jsonNode.isObject()) {
            return data;
        }
        for (Map.Entry<String, OutputFormatField> entry : outputFormat.entrySet()) {
            String key = entry.getKey();
            OutputFormatField field = entry.getValue();
            if (jsonNode.has(key)) {
                data.put(key, parseJsonNodeRecursively(jsonNode.get(key), field));
            } else {
                data.put(key, null);
            }
        }
        return data;
    }


    private int countMissingRequiredFields(Map<String, Object> data, Map<String, OutputFormatField> outputFormat) {
        int missing = 0;
        for (Map.Entry<String, OutputFormatField> entry : outputFormat.entrySet()) {
            String key = entry.getKey();
            OutputFormatField field = entry.getValue();
            if (Boolean.TRUE.equals(field.getRequired()) && (data == null || data.get(key) == null)) {
                log.warn("【算法】缺少必填字段: {}", key);
                missing++;
            }
        }
        return missing;
    }


    private Object parseJsonNodeRecursively(JsonNode node, OutputFormatField field) {
        if (node == null || node.isNull()) {
            return null;
        }
        String type = field != null ? field.getType() : null;
        if (type == null) {
            return autoConvertJsonNode(node);
        }
        return switch (type) {
            case "int" -> node.isNumber() ? node.intValue() : Integer.parseInt(node.asText());
            case "double" -> node.isNumber() ? node.doubleValue() : Double.parseDouble(node.asText());
            case "bool" -> node.isBoolean() ? node.booleanValue() : Boolean.parseBoolean(node.asText());
            case "string" -> node.asText();
            case "enum" -> autoConvertJsonNode(node);
            case "array" -> parseArrayNode(node, field.getItems());
            case "object" -> parseObjectNode(node, field.getProperties());
            default -> autoConvertJsonNode(node);
        };
    }


    private List<Object> parseArrayNode(JsonNode node, OutputFormatField itemField) {
        List<Object> list = new ArrayList<>();
        for (JsonNode item : node) {
            list.add(parseJsonNodeRecursively(item, itemField));
        }
        return list;
    }


    private Map<String, Object> parseObjectNode(JsonNode node, Map<String, OutputFormatField> properties) {
        Map<String, Object> map = new LinkedHashMap<>();
        node.fields().forEachRemaining(entry -> {
            OutputFormatField propField = properties != null ? properties.get(entry.getKey()) : null;
            map.put(entry.getKey(), parseJsonNodeRecursively(entry.getValue(), propField));
        });
        return map;
    }


    private Object autoConvertJsonNode(JsonNode node) {
        if (node.isNull()) return null;
        if (node.isBoolean()) return node.booleanValue();
        if (node.isInt()) return node.intValue();
        if (node.isLong()) return node.longValue();
        if (node.isNumber()) return node.doubleValue();
        if (node.isTextual()) return node.asText();
        if (node.isArray()) return parseArrayNode(node, null);
        if (node.isObject()) return parseObjectNode(node, null);
        return node;
    }


    private String formatOutputAsText(Map<String, Object> data, Map<String, OutputFormatField> outputFormat) {
        if (outputFormat == null || outputFormat.isEmpty()) {
            return data != null ? data.toString() : "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, OutputFormatField> entry : outputFormat.entrySet()) {
            String key = entry.getKey();
            OutputFormatField field = entry.getValue();
            Object value = data != null ? data.get(key) : null;

            if (!sb.isEmpty()) {
                sb.append("\n");
            }
            sb.append(field.getName()).append("：");
            if (value == null) {
                sb.append("无");
            } else if ("array".equals(field.getType()) || "object".equals(field.getType())) {
                sb.append(JsonUtil.toJson(value));
            } else {
                sb.append(value);
            }
        }
        return sb.toString();
    }


    private Map<String, Object> createDefaultOutput(Map<String, OutputFormatField> outputFormat) {
        Map<String, Object> result = new HashMap<>();
        if (outputFormat == null) {
            return result;
        }
        for (Map.Entry<String, OutputFormatField> entry : outputFormat.entrySet()) {
            result.put(entry.getKey(), getDefaultValue(entry.getValue()));
        }
        return result;
    }


    private Object getDefaultValue(OutputFormatField field) {
        if (field == null) {
            return null;
        }
        return switch (field.getType()) {
            case "int" -> 0;
            case "double" -> 0.0;
            case "bool" -> false;
            case "string", "enum" -> "";
            case "array" -> new ArrayList<>();
            case "object" -> new LinkedHashMap<>();
            default -> null;
        };
    }

    private Map<String, Object> executeVisionAlgorithm(Algorithm algorithm) {
        if (StrUtil.isBlank(algorithm.getInferenceUrl())) {
            throw new ServiceException("视觉模型算法未配置推理接口地址");
        }

        Map<String, Object> outputData = new HashMap<>();
        outputData.put("detections", "这是模拟的检测结果，实际需要调用真实的视觉模型推理服务");
        outputData.put("model_format", algorithm.getModelFormat());
        outputData.put("inference_url", algorithm.getInferenceUrl());

        log.info("【算法】执行视觉模型算法: algorithmCode={}, inferenceUrl={}",
                algorithm.getAlgorithmCode(), algorithm.getInferenceUrl());

        return outputData;
    }

    private void validateAlgorithmConfig(String algorithmType, AlgorithmCreateRequest request) {
        if (AlgorithmType.LLM.getCode().equals(algorithmType)) {
            if (request.getLlmModelId() == null) {
                throw new ServiceException("大模型类算法必须配置大模型ID");
            }
            if (StrUtil.isBlank(request.getDetectionContent())) {
                throw new ServiceException("大模型类算法必须配置检测内容");
            }
        } else if (AlgorithmType.VISION.getCode().equals(algorithmType)) {
            if (StrUtil.isBlank(request.getModelFormat())) {
                throw new ServiceException("视觉模型算法必须配置模型格式");
            }
            if (StrUtil.isBlank(request.getInferenceUrl())) {
                throw new ServiceException("视觉模型算法必须配置推理接口地址");
            }
        } else {
            throw new ServiceException("不支持的算法类型: " + algorithmType);
        }
    }


    private Map<String, OutputFormatField> getDefaultOutputFormat() {
        Map<String, OutputFormatField> defaultFormat = new LinkedHashMap<>();

        OutputFormatField isAlarmField = new OutputFormatField();
        isAlarmField.setName("是否告警");
        isAlarmField.setType("bool");
        isAlarmField.setDesc("true表示检测到目标，false表示未检测到");
        isAlarmField.setRequired(true);
        isAlarmField.setExampleValue(true);
        defaultFormat.put("isAlarm", isAlarmField);

        OutputFormatField countField = new OutputFormatField();
        countField.setName("数量");
        countField.setType("int");
        countField.setDesc("检测到的目标区域数量（0表示未检测到）");
        countField.setRequired(true);
        countField.setExampleValue(2);
        defaultFormat.put("count", countField);

        OutputFormatField confidenceField = new OutputFormatField();
        confidenceField.setName("置信度");
        confidenceField.setType("int");
        confidenceField.setDesc("0-100之间的整数");
        confidenceField.setRequired(true);
        confidenceField.setExampleValue(70);
        defaultFormat.put("confidence", confidenceField);

        OutputFormatField coordinatesField = new OutputFormatField();
        coordinatesField.setName("坐标");
        coordinatesField.setType("array");
        coordinatesField.setDesc("每个目标区域的归一化边界框坐标，格式为 [[x1,y1,x2,y2], ...]，取值范围 0-1，其中 (x1,y1) 是左上角坐标，(x2,y2) 是右下角坐标");
        coordinatesField.setRequired(true);
        coordinatesField.setExampleValue(List.of(
                List.of(0.12, 0.34, 0.56, 0.78),
                List.of(0.87, 0.65, 0.43, 0.21)
        ));

        OutputFormatField coordinateItemField = new OutputFormatField();
        coordinateItemField.setType("array");

        OutputFormatField doubleField = new OutputFormatField();
        doubleField.setType("double");
        doubleField.setDesc("归一化坐标值（0-1），格式为 [x1, y1, x2, y2]");
        coordinateItemField.setItems(doubleField);

        coordinatesField.setItems(coordinateItemField);
        defaultFormat.put("coordinates", coordinatesField);

        OutputFormatField descriptionField = new OutputFormatField();
        descriptionField.setName("说明");
        descriptionField.setType("string");
        descriptionField.setDesc("描述每个目标区域的位置、大小、颜色等特征");
        descriptionField.setRequired(false);
        descriptionField.setExampleValue("检测到两处目标，分别位于图片的左上角区域和右上角区域");
        defaultFormat.put("description", descriptionField);

        return defaultFormat;
    }


    private String drawBoundingBoxesIfNeeded(Map<String, Object> outputData, String imageUrl, String algorithmCode, String requestId) {
        try {
            Map<String, Object> parseResult = (Map<String, Object>) outputData.get("result");
            Map<String, Object> data = parseResult != null ? (Map<String, Object>) parseResult.get("data") : null;
            Object isAlarmObj = data != null ? data.get("isAlarm") : null;
            Object coordinatesObj = data != null ? data.get("coordinates") : null;

            boolean isAlarm = false;
            if (isAlarmObj instanceof Boolean) {
                isAlarm = (Boolean) isAlarmObj;
            } else if (isAlarmObj instanceof String) {
                isAlarm = Boolean.parseBoolean((String) isAlarmObj);
            }

            boolean isEmptyCoordinates = true;
            if (coordinatesObj instanceof List<?> list) {
                isEmptyCoordinates = list.isEmpty();
            }

            boolean hasDetection = isAlarm && coordinatesObj != null && !isEmptyCoordinates;

            if (hasDetection) {
                String objectName = drawBoundingBoxes(imageUrl, coordinatesObj, algorithmCode, requestId);
                log.debug("【算法】检测到物体，返回画框图对象名称");
                return objectName;
            } else {
                log.debug("【算法】未检测到物体，无需画框");
                return null;
            }
        } catch (Exception e) {
            log.error("【算法】结果图处理失败: {}", e.getMessage(), e);
            return null;
        }
    }


    private String drawBoundingBoxes(String imageUrl, Object coordinatesObj, String algorithmCode, String requestId) {
        try {
            byte[] imageBytes = loadImageBytes(imageUrl);

            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (originalImage == null) {
                log.warn("【算法】无法读取图片格式, url={}", imageUrl);
                return null;
            }

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            BufferedImage annotatedImage = new BufferedImage(width, height, originalImage.getType());
            Graphics2D g2d = annotatedImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3));
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (coordinatesObj instanceof List<?> coordinates) {
                for (Object coordObj : coordinates) {
                    if (coordObj instanceof List<?> coordList && coordList.size() >= 4) {
                        double x1 = ((Number) coordList.get(0)).doubleValue() * width;
                        double y1 = ((Number) coordList.get(1)).doubleValue() * height;
                        double x2 = ((Number) coordList.get(2)).doubleValue() * width;
                        double y2 = ((Number) coordList.get(3)).doubleValue() * height;

                        int rx = (int) Math.min(x1, x2);
                        int ry = (int) Math.min(y1, y2);
                        int rw = (int) Math.abs(x2 - x1);
                        int rh = (int) Math.abs(y2 - y1);

                        g2d.drawRect(rx, ry, rw, rh);
                    }
                }
            }
            g2d.dispose();

            String format = getImageFormat(imageUrl);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(annotatedImage, format, baos);
            byte[] annotatedBytes = baos.toByteArray();

            String objectName = "algorithm-result/" + algorithmCode + "/" + requestId + "/" + System.currentTimeMillis() + "." + format;
            minioService.uploadBytes(objectName, annotatedBytes, null);

            log.info("【算法】检测结果图已上传: objectName={}", objectName);
            return objectName;

        } catch (Exception e) {
            log.error("【算法】画框失败: {}", e.getMessage(), e);
            return null;
        }
    }

    private byte[] loadImageBytes(String imageUrl) {
        String objectName = resolveProxyMinioObjectName(imageUrl);
        if (StrUtil.isNotBlank(objectName)) {
            return minioService.downloadAsBytes(objectName);
        }

        try (HttpResponse response = HttpRequest.get(imageUrl).timeout(10000).execute()) {
            if (response.getStatus() != 200) {
                log.warn("【算法】下载原图失败, status={}, url={}", response.getStatus(), imageUrl);
                throw new SystemException("下载原图失败, status=" + response.getStatus());
            }
            return response.bodyBytes();
        }
    }

    private String resolveProxyMinioObjectName(String imageUrl) {
        if (StrUtil.isBlank(imageUrl)) {
            return null;
        }
        String path = extractPath(imageUrl);
        if (!path.startsWith("/minio/")) {
            return null;
        }
        String bucketAndObject = path.substring("/minio/".length());
        int slashIndex = bucketAndObject.indexOf('/');
        if (slashIndex < 0 || slashIndex == bucketAndObject.length() - 1) {
            return null;
        }
        return URLDecoder.decode(bucketAndObject.substring(slashIndex + 1), StandardCharsets.UTF_8);
    }

    private String extractPath(String imageUrl) {
        try {
            return URI.create(imageUrl.trim()).getPath();
        } catch (IllegalArgumentException e) {
            int queryIndex = imageUrl.indexOf('?');
            return queryIndex >= 0 ? imageUrl.substring(0, queryIndex) : imageUrl;
        }
    }


    private String getImageFormat(String imageUrl) {
        if (imageUrl != null) {
            String lower = imageUrl.toLowerCase();
            if (lower.endsWith(".png")) return "png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "jpg";
            if (lower.endsWith(".gif")) return "gif";
            if (lower.endsWith(".bmp")) return "bmp";
        }
        return "png";
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> extractResultForLog(Map<String, Object> outputData) {
        if (outputData == null) {
            return new HashMap<>();
        }
        Map<String, Object> result = (Map<String, Object>) outputData.get("result");
        if (result == null) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(result), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("【算法】深拷贝 outputData result 失败: {}", e.getMessage());
            return new HashMap<>(result);
        }
    }


    @SuppressWarnings("unchecked")
    private Integer extractIsAlarm(Map<String, Object> outputData) {
        if (outputData == null) {
            return 0;
        }
        Map<String, Object> result = (Map<String, Object>) outputData.get("result");
        if (result == null) {
            return 0;
        }
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        if (data == null) {
            return 0;
        }
        Object alarmObj = data.get("isAlarm");
        return (alarmObj instanceof Boolean && (Boolean) alarmObj) ? 1 : 0;
    }
}
