package com.unicom.visionhub.web.controller;

import com.unicom.visionhub.common.config.MinioProperties;
import com.unicom.visionhub.common.enums.FileBusinessType;
import com.unicom.visionhub.common.exception.SystemException;
import com.unicom.visionhub.common.response.Result;
import com.unicom.visionhub.web.service.FileUploadLogService;
import com.unicom.visionhub.web.service.MinioService;
import com.unicom.visionhub.web.validation.FileUploadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "文件上传", description = "通用文件上传接口")
public class FileUploadController {

    private final MinioService minioService;
    private final FileUploadLogService fileUploadLogService;
    private final MinioProperties minioProperties;


    @Data
    public static class FileUploadRequest {

        @NotBlank(message = "业务类型不能为空")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "业务类型只能包含小写字母、数字和连字符")
        @Parameter(description = "业务类型（如 llm-icon, algorithm-icon, algorithm-test-image）", required = true, example = "llm-icon")
        private String businessType;
    }


    @PostMapping("/api/files/upload")
    @Operation(summary = "通用文件上传", description = "按业务类型上传文件到指定目录")
    public Result<Map<String, String>> uploadFile(
            @Parameter(description = "文件", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "业务类型（如 llm-icon, algorithm-icon, algorithm-test-image）", required = true)
            @RequestPart("businessType") String businessType,
            HttpServletRequest request) {

        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        log.info("【文件上传】上传文件: businessType={}, fileName={}, ip={}", businessType, file.getOriginalFilename(), ipAddress);

        FileBusinessType businessTypeEnum = FileBusinessType.fromCode(businessType);
        if (businessTypeEnum == null) {
            fileUploadLogService.logUploadFailure(businessType, file, "业务类型不支持", ipAddress, userAgent);
            throw new SystemException("业务类型不支持");
        }

        try {
            if (businessTypeEnum == FileBusinessType.LLM_ICON || businessTypeEnum == FileBusinessType.ALGORITHM_ICON
                    || businessTypeEnum == FileBusinessType.ALGORITHM_TEST_IMAGE) {
                FileUploadUtil.validateImageFile(file);
            }
        } catch (Exception e) {
            fileUploadLogService.logUploadFailure(businessType, file, e.getMessage(), ipAddress, userAgent);
            throw e;
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String objectName = businessType + "/" + System.currentTimeMillis() + extension;

            String fileUrl = minioService.uploadFile(file, objectName);

            if (businessTypeEnum == FileBusinessType.ALGORITHM_TEST_IMAGE) {
                minioService.setFileExpiry(1, businessType + "/", null);
            }

            String bucketName = getBucketName();

            fileUploadLogService.logUploadSuccess(businessType, file, objectName, fileUrl, bucketName, ipAddress, userAgent);

            Map<String, String> result = new HashMap<>();
            result.put("path", objectName);
            result.put("url", minioService.getFileUrlWithCdn(objectName));

            log.info("【文件上传】文件上传成功: businessType={}, objectName={}", businessType, objectName);
            return Result.success("文件上传成功", result);
        } catch (Exception e) {
            fileUploadLogService.logUploadFailure(businessType, file, e.getMessage(), ipAddress, userAgent);
            log.error("【文件上传】文件上传失败: businessType={}", businessType, e);
            throw new SystemException("文件上传失败: " + e.getMessage());
        }
    }


    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }


    private String getBucketName() {
        return minioProperties.getBucketName();
    }
}
