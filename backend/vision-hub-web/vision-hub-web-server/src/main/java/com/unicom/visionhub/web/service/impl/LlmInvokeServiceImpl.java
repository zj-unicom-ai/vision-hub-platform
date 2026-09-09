package com.unicom.visionhub.web.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.unicom.visionhub.common.config.MinioProperties;
import com.unicom.visionhub.common.enums.ImageTransferMode;
import com.unicom.visionhub.common.enums.ModelVersion;
import com.unicom.visionhub.common.enums.Provider;
import com.unicom.visionhub.common.exception.SystemException;
import com.unicom.visionhub.web.dto.FileMetadata;
import com.unicom.visionhub.web.entity.model.LlmModel;
import com.unicom.visionhub.web.service.LlmInvokeService;
import com.unicom.visionhub.web.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class LlmInvokeServiceImpl implements LlmInvokeService {

    private static final int DEFAULT_MAX_TOKENS = 4096;
    private static final int CONNECT_TIMEOUT = 60000;
    private static final int READ_TIMEOUT = 300000;

    private final MinioService minioService;
    private final MinioProperties minioProperties;

    @Override
    public Map<String, Object> invoke(LlmModel llmModel, String prompt, Integer maxTokens) {
        Integer actualMaxTokens = maxTokens != null ? maxTokens : llmModel.getMaxTokens();
        if (actualMaxTokens == null || actualMaxTokens <= 0) {
            actualMaxTokens = DEFAULT_MAX_TOKENS;
        }

        Provider provider = Provider.fromCode(llmModel.getProvider());
        if (provider == null) {
            log.error("【大模型调用】不支持的供应商: {}", llmModel.getProvider());
            throw new SystemException("不支持的大模型供应商: " + llmModel.getProvider());
        }

        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> llmResult;
            switch (provider) {
                case OPENAI:
                case QWEN:
                case YUANJING:
                    llmResult = invokeOpenAiCompatible(llmModel, prompt, actualMaxTokens);
                    break;
                default:
                    throw new SystemException("不支持的大模型供应商: " + provider.getDescription());
            }

            long duration = System.currentTimeMillis() - startTime;
            String response = (String) llmResult.get("content");
            log.info("【大模型调用】调用成功, provider={}, model={}, duration={}ms",
                    provider.getCode(), llmModel.getModelId(), duration);

            result.put("success", true);
            result.put("response", response);
            result.put("model", llmModel.getModelId());
            result.put("provider", provider.getCode());
            result.put("duration", duration);
            result.put("promptTokens", llmResult.get("promptTokens"));
            result.put("completionTokens", llmResult.get("completionTokens"));
            result.put("totalTokens", llmResult.get("totalTokens"));

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("【大模型调用】调用失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("duration", duration);
            throw new SystemException("大模型调用失败: " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public Map<String, Object> invoke(LlmModel llmModel, String prompt) {
        return invoke(llmModel, prompt, (Integer) null);
    }

    @Override
    public Map<String, Object> invoke(LlmModel llmModel, String prompt, String imageUrl, Integer maxTokens) {
        Integer actualMaxTokens = maxTokens != null ? maxTokens : llmModel.getMaxTokens();
        if (actualMaxTokens == null || actualMaxTokens <= 0) {
            actualMaxTokens = DEFAULT_MAX_TOKENS;
        }

        Provider provider = Provider.fromCode(llmModel.getProvider());
        if (provider == null) {
            log.error("【大模型调用】不支持的供应商: {}", llmModel.getProvider());
            throw new SystemException("不支持的大模型供应商: " + llmModel.getProvider());
        }

        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> llmResult;
            switch (provider) {
                case OPENAI:
                case QWEN:
                case YUANJING:
                    llmResult = invokeOpenAiCompatibleWithImage(llmModel, prompt, imageUrl, actualMaxTokens);
                    break;
                default:
                    throw new SystemException("不支持的大模型供应商: " + provider.getDescription());
            }

            long duration = System.currentTimeMillis() - startTime;
            String response = (String) llmResult.get("content");
            log.info("【大模型调用】调用成功, provider={}, model={}, duration={}ms",
                    provider.getCode(), llmModel.getModelId(), duration);

            result.put("success", true);
            result.put("response", response);
            result.put("model", llmModel.getModelId());
            result.put("provider", provider.getCode());
            result.put("duration", duration);
            result.put("promptTokens", llmResult.get("promptTokens"));
            result.put("completionTokens", llmResult.get("completionTokens"));
            result.put("totalTokens", llmResult.get("totalTokens"));

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("【大模型调用】调用失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("duration", duration);
            throw new SystemException("大模型调用失败: " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public Map<String, Object> invoke(LlmModel llmModel, String prompt, String imageUrl) {
        return invoke(llmModel, prompt, imageUrl, null);
    }


    private Map<String, Object> invokeOpenAiCompatible(LlmModel llmModel, String prompt, Integer maxTokens) {
        Object messages = JSONUtil.createArray()
                .put(JSONUtil.createObj().set("role", "user").set("content", prompt));
        return doInvokeOpenAiCompatible(llmModel, messages, maxTokens);
    }


    private Map<String, Object> invokeOpenAiCompatibleWithImage(LlmModel llmModel, String prompt, String imageUrl, Integer maxTokens) {
        String actualImageUrl = imageUrl;

        ImageTransferMode transferMode = ImageTransferMode.fromCode(llmModel.getImageTransferMode());
        if (transferMode == ImageTransferMode.BASE64) {
            actualImageUrl = convertImageToBase64(imageUrl);
            log.debug("【大模型调用】图片已转换为base64格式");
        }

        JSONObject textContent = JSONUtil.createObj().set("type", "text").set("text", prompt);
        JSONObject imageContent = JSONUtil.createObj()
                .set("type", "image_url")
                .set("image_url", JSONUtil.createObj().set("url", actualImageUrl));

        Object messages = JSONUtil.createArray()
                .put(JSONUtil.createObj()
                        .set("role", "user")
                        .set("content", JSONUtil.createArray().put(textContent).put(imageContent)));

        return doInvokeOpenAiCompatible(llmModel, messages, maxTokens);
    }


    private String convertImageToBase64(String imageUrl) {
        try {
            ImageContent imageContent = loadImageContent(imageUrl);

            String base64 = Base64.getEncoder().encodeToString(imageContent.bytes());
            return "data:" + imageContent.contentType() + ";base64," + base64;
        } catch (SystemException e) {
            throw e;
        } catch (Exception e) {
            log.error("【大模型调用】图片转base64失败: {}", imageUrl, e);
            throw new SystemException("图片转base64失败: " + e.getMessage());
        }
    }

    private ImageContent loadImageContent(String imageUrl) {
        String objectName = resolveMinioObjectName(imageUrl);
        if (objectName != null) {
            byte[] imageBytes = minioService.downloadAsBytes(objectName);
            return new ImageContent(imageBytes, getMinioContentType(objectName));
        }

        HttpResponse response = HttpRequest.get(imageUrl)
                .timeout(CONNECT_TIMEOUT)
                .execute();

        if (response.getStatus() != 200) {
            throw new SystemException("下载图片失败, status=" + response.getStatus() + ", url=" + imageUrl);
        }

        String contentType = response.header("Content-Type");
        if (contentType == null || contentType.isEmpty()) {
            contentType = guessContentType(imageUrl);
        }
        return new ImageContent(response.bodyBytes(), contentType);
    }

    private String resolveMinioObjectName(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        String path = extractPath(imageUrl.trim());
        if (path == null || path.isBlank()) {
            return null;
        }

        String bucketPrefix = "/" + minioProperties.getBucketName() + "/";
        String minioProxyPrefix = "/minio" + bucketPrefix;
        String cdnPathPrefix = buildCdnPathPrefix();

        if (path.startsWith(minioProxyPrefix)) {
            return decodeObjectName(path.substring(minioProxyPrefix.length()));
        }
        if (cdnPathPrefix != null && path.startsWith(cdnPathPrefix)) {
            return decodeObjectName(path.substring(cdnPathPrefix.length()));
        }
        if (path.startsWith(bucketPrefix)) {
            return decodeObjectName(path.substring(bucketPrefix.length()));
        }
        return null;
    }

    private String extractPath(String imageUrl) {
        try {
            String path = URI.create(imageUrl).getPath();
            if (path != null) {
                return path;
            }
        } catch (IllegalArgumentException ignored) {
            // Fallback below handles plain paths with illegal URI characters.
        }
        int queryIndex = imageUrl.indexOf('?');
        String withoutQuery = queryIndex >= 0 ? imageUrl.substring(0, queryIndex) : imageUrl;
        int fragmentIndex = withoutQuery.indexOf('#');
        return fragmentIndex >= 0 ? withoutQuery.substring(0, fragmentIndex) : withoutQuery;
    }

    private String buildCdnPathPrefix() {
        String cdn = minioProperties.getCdn();
        if (cdn == null || cdn.isBlank()) {
            return null;
        }
        String cdnPath = extractPath(cdn.trim());
        if (cdnPath == null || cdnPath.isBlank() || "/".equals(cdnPath)) {
            cdnPath = "";
        }
        while (cdnPath.endsWith("/")) {
            cdnPath = cdnPath.substring(0, cdnPath.length() - 1);
        }
        return cdnPath + "/" + minioProperties.getBucketName() + "/";
    }

    private String decodeObjectName(String objectName) {
        return URLDecoder.decode(objectName, StandardCharsets.UTF_8);
    }

    private String getMinioContentType(String objectName) {
        try {
            FileMetadata metadata = minioService.getFileMetadata(objectName);
            if (metadata.getContentType() != null && !metadata.getContentType().isBlank()) {
                return metadata.getContentType();
            }
        } catch (Exception e) {
            log.debug("【大模型调用】获取MinIO文件类型失败，使用后缀推断: {}", objectName, e);
        }
        return guessContentType(objectName);
    }

    private String guessContentType(String imageUrl) {
        String lower = imageUrl.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".bmp")) {
            return "image/bmp";
        }
        return "image/jpeg";
    }

    private record ImageContent(byte[] bytes, String contentType) {
    }


    private Map<String, Object> doInvokeOpenAiCompatible(LlmModel llmModel, Object messages, Integer maxTokens) {
        String apiUrl = llmModel.getApiUrl();
        if (!apiUrl.endsWith("/")) {
            apiUrl += "/";
        }
        apiUrl += "chat/completions";

        JSONObject requestBody = JSONUtil.createObj()
                .set("model", llmModel.getModelId())
                .set("messages", messages)
                .set("max_tokens", maxTokens)
                .set("temperature", 0);

        boolean isQwenHighVersion = ModelVersion.isQwenHighVersion(llmModel.getProvider(), llmModel.getModelVersion());
        if (isQwenHighVersion) {
            requestBody.set("chat_template_kwargs", JSONUtil.createObj().set("enable_thinking", false));
        } else {
            requestBody.set("enable_thinking", false);
        }

        log.debug("【大模型调用】请求URL: {}", apiUrl);
        log.debug("【大模型调用】请求体: {}", truncateMediaContent(requestBody.toString()));

        HttpResponse response = HttpRequest.post(apiUrl)
                .header("Authorization", "Bearer " + llmModel.getApiKey())
                .header("Content-Type", "application/json")
                .timeout(CONNECT_TIMEOUT)
                .body(requestBody.toString())
                .execute();

        int status = response.getStatus();
        String responseBody = response.body();

        log.debug("【大模型调用】响应状态: {}, 响应体: {}", status, truncateMediaContent(responseBody));

        if (status != 200) {
            throw new SystemException("大模型API调用失败, status=" + status + ", response=" + truncateMediaContent(responseBody));
        }

        try {
            JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
            String content = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getStr("content");

            JSONObject usage = jsonResponse.getJSONObject("usage");
            if (usage != null) {
                log.info("【大模型调用】Token消耗: promptTokens={}, completionTokens={}, totalTokens={}",
                        usage.getInt("prompt_tokens", 0),
                        usage.getInt("completion_tokens", 0),
                        usage.getInt("total_tokens", 0));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("content", content);
            if (usage != null) {
                result.put("promptTokens", usage.getInt("prompt_tokens", 0));
                result.put("completionTokens", usage.getInt("completion_tokens", 0));
                result.put("totalTokens", usage.getInt("total_tokens", 0));
            }
            return result;
        } catch (Exception e) {
            log.error("【大模型调用】解析响应失败: {}", truncateMediaContent(responseBody), e);
            throw new SystemException("解析大模型响应失败: " + e.getMessage());
        }
    }


    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int chineseChars = 0;
        int otherChars = 0;
        for (char c : text.toCharArray()) {
            if (isChinese(c)) {
                chineseChars++;
            } else {
                otherChars++;
            }
        }
        return (int) (chineseChars / 1.5 + otherChars / 4.0);
    }

    private boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }


    private String truncateMediaContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        String result = content;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(data:[^;]+;base64,)([A-Za-z0-9+/=]{100,})");
        java.util.regex.Matcher matcher = pattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1) + "[base64已截断，长度" + matcher.group(2).length() + "字节]");
        }
        matcher.appendTail(sb);
        result = sb.toString();
        result = result.replaceAll("(image_url.*?\"url\"\\s*:\\s*\")https?://[^\"]{80,}", "$1[图片URL已截断]");
        return result;
    }
}
