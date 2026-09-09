package com.unicom.visionhub.web.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import com.unicom.visionhub.common.config.MinioProperties;
import com.unicom.visionhub.common.exception.SystemException;
import com.unicom.visionhub.web.dto.FileMetadata;
import com.unicom.visionhub.web.dto.MinioResp;
import com.unicom.visionhub.web.service.MinioService;
import io.minio.BucketExistsArgs;
import io.minio.GetBucketLifecycleArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectTagsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketLifecycleArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.SetObjectTagsArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.AndOperator;
import io.minio.messages.Expiration;
import io.minio.messages.LifecycleConfiguration;
import io.minio.messages.LifecycleRule;
import io.minio.messages.ResponseDate;
import io.minio.messages.RuleFilter;
import io.minio.messages.Status;
import io.minio.messages.Tags;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    private static final String RULE_ID_SEPARATOR = "_";

    @PostConstruct
    public void init() {
        ensureBucketExists();
        setFileExpiry(1, "algorithm-temp/", null);
        setFileExpiry(1, "algorithm-temp/", null);
        log.info("【MinIO】初始化完成，已配置 *-temp/ 前缀 1 天过期");
    }


    @Override
    public String uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String objectName = UUID.randomUUID().toString() + extension;
        return uploadFile(file, objectName);
    }

    @Override
    public String uploadFile(MultipartFile file, String objectName) {
        try {
            InputStream inputStream = file.getInputStream();
            long size = file.getSize();
            String contentType = file.getContentType();
            return uploadFile(inputStream, objectName, size, contentType);
        } catch (IOException e) {
            log.error("【MinIO】读取文件失败: {}", objectName, e);
            throw new SystemException("读取文件失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String objectName, long size, String contentType) {
        try {
            ensureBucketExists();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );

            log.info("【MinIO】文件上传成功: bucket={}, object={}", minioProperties.getBucketName(), objectName);
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("【MinIO】文件上传失败: {}", objectName, e);
            throw new SystemException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public MinioResp uploadFileWithResp(MultipartFile file, String objectName) {
        try {
            ensureBucketExists();

            byte[] uploadBytes = file.getBytes();
            String fileMd5 = DigestUtil.md5Hex(uploadBytes);
            ByteArrayInputStream bais = new ByteArrayInputStream(uploadBytes);
            String contentType = HttpUtil.getMimeType(objectName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .stream(bais, uploadBytes.length, -1)
                            .contentType(contentType)
                            .build()
            );

            MinioResp response = MinioResp.builder()
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .fileMd5(fileMd5)
                    .objectName(objectName)
                    .bucketName(minioProperties.getBucketName())
                    .url(getFileUrl(objectName))
                    .build();

            log.info("【MinIO】文件上传成功: bucket={}, object={}", minioProperties.getBucketName(), objectName);
            return response;
        } catch (Exception e) {
            log.error("【MinIO】文件上传失败: {}", objectName, e);
            throw new SystemException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadBytes(String objectName, byte[] body, Map<String, String> tags) {
        try {
            ensureBucketExists();

            ByteArrayInputStream bais = new ByteArrayInputStream(body);
            String contentType = HttpUtil.getMimeType(objectName);

            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .stream(bais, body.length, -1)
                    .contentType(contentType);

            if (tags != null && !tags.isEmpty()) {
                builder.tags(tags);
            }

            minioClient.putObject(builder.build());

            log.info("【MinIO】字节数组上传成功: bucket={}, object={}", minioProperties.getBucketName(), objectName);
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("【MinIO】字节数组上传失败: {}", objectName, e);
            throw new SystemException("字节数组上传失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(String objectName, File file) {
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            ensureBucketExists();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .contentType(HttpUtil.getMimeType(objectName))
                            .stream(inputStream, file.length(), -1)
                            .build()
            );

            log.info("【MinIO】文件上传成功: bucket={}, object={}", minioProperties.getBucketName(), objectName);
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("【MinIO】文件上传失败: {}", objectName, e);
            throw new SystemException("文件上传失败: " + e.getMessage());
        }
    }


    @Override
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("【MinIO】文件下载失败: {}", objectName, e);
            throw new SystemException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] downloadAsBytes(String objectName) {
        try (InputStream inputStream = downloadFile(objectName)) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            log.error("【MinIO】读取文件流失败: {}", objectName, e);
            throw new SystemException("读取文件流失败: " + e.getMessage());
        }
    }


    @Override
    public void deleteFile(String objectName) {
        remove(minioProperties.getBucketName(), objectName);
    }

    @Override
    public void remove(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("【MinIO】文件删除成功: bucket={}, object={}", bucketName, objectName);
        } catch (Exception e) {
            log.error("【MinIO】文件删除失败: bucket={}, object={}", bucketName, objectName, e);
            throw new SystemException("文件删除失败: " + e.getMessage());
        }
    }


    @Override
    public String getFileUrl(String objectName) {
        return buildFileUrl(minioProperties.getEndpoint(), objectName);
    }

    @Override
    public String getPresignedUrl(String objectName, Integer expires) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .expiry(expires)
                            .build()
            );
        } catch (Exception e) {
            log.error("【MinIO】获取临时URL失败: {}", objectName, e);
            throw new SystemException("获取临时URL失败: " + e.getMessage());
        }
    }

    @Override
    public String getPresignedUrl(String objectName) {
        return getPresignedUrl(objectName, minioProperties.getDefaultExpiry());
    }


    @Override
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new SystemException("检查文件失败: " + e.getMessage());
        } catch (Exception e) {
            throw new SystemException("检查文件失败: " + e.getMessage());
        }
    }

    @Override
    public FileMetadata getFileMetadata(String objectName) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .build()
            );

            return FileMetadata.builder()
                    .objectName(objectName)
                    .size(stat.size())
                    .contentType(stat.contentType())
                    .lastModified(LocalDateTime.ofInstant(stat.lastModified().toInstant(), ZoneId.systemDefault()))
                    .etag(stat.etag())
                    .versionId(stat.versionId())
                    .build();
        } catch (Exception e) {
            log.error("【MinIO】获取文件元数据失败: {}", objectName, e);
            throw new SystemException("获取文件元数据失败: " + e.getMessage());
        }
    }


    @Override
    public void updateTags(String objectName, Map<String, String> tags) {
        try {
            SetObjectTagsArgs.Builder builder = SetObjectTagsArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .tags(tags != null ? tags : new HashMap<>());

            minioClient.setObjectTags(builder.build());
            log.info("【MinIO】文件标签更新成功: objectName={}", objectName);
        } catch (Exception e) {
            log.error("【MinIO】更新文件标签失败: {}", objectName, e);
            throw new SystemException("更新文件标签失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, String> getFileTags(String objectName) {
        try {
            Tags tags = minioClient.getObjectTags(
                    GetObjectTagsArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .build()
            );
            return new HashMap<>(tags.get());
        } catch (Exception e) {
            log.error("【MinIO】获取文件标签失败: {}", objectName, e);
            throw new SystemException("获取文件标签失败: " + e.getMessage());
        }
    }


    @Override
    public void setFileExpiry(Integer expireDays, String prefix, Map<String, String> tags) {
        Expiration expiration = new Expiration((ResponseDate) null, expireDays, null);
        upsertExpiry(expiration, prefix, tags);
    }

    @Override
    public void setFileExpiry(Date expireDate, String prefix, Map<String, String> tags) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(expireDate.toInstant(), ZoneId.of("Asia/Shanghai"));
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Shanghai"));
        Expiration expiration = new Expiration(zonedDateTime, null, null);
        upsertExpiry(expiration, prefix, tags);
    }

    private void upsertExpiry(Expiration expiration, String prefix, Map<String, String> tags) {
        LifecycleConfiguration lifecycleConfiguration;
        try {
            lifecycleConfiguration = getBucketLifecycle();
        } catch (Exception e) {
            log.error("【MinIO】查询原有规则失败: {}", e.getMessage());
            return;
        }

        LifecycleRule rule = buildLifecycleRule(expiration, prefix, tags);

        if (lifecycleConfiguration == null) {
            lifecycleConfiguration = new LifecycleConfiguration(Collections.singletonList(rule));
        } else {
            List<LifecycleRule> newRules = new ArrayList<>();
            for (LifecycleRule lifecycleRule : lifecycleConfiguration.rules()) {
                if (StringUtils.equals(lifecycleRule.id(), rule.id())) {
                    continue;
                }
                newRules.add(lifecycleRule);
            }
            newRules.add(rule);
            lifecycleConfiguration = new LifecycleConfiguration(newRules);
        }

        try {
            minioClient.setBucketLifecycle(
                    SetBucketLifecycleArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .config(lifecycleConfiguration)
                            .build()
            );
            log.info("【MinIO】文件过期时间设置成功");
        } catch (Exception e) {
            log.error("【MinIO】设置过期时间失败: {}", e.getMessage());
            throw new SystemException("设置过期时间失败: " + e.getMessage());
        }
    }

    private LifecycleRule buildLifecycleRule(Expiration expiration, String prefix, Map<String, String> tags) {
        String suffix = "EXPIRATION";
        RuleFilter filter;
        StringBuilder ruleIdBuilder = new StringBuilder();

        if (StringUtils.isNotBlank(prefix) && tags != null && !tags.isEmpty()) {
            AndOperator andOperator = new AndOperator(prefix, tags);
            filter = new RuleFilter(andOperator, null, null);
            prefix = prefix.replace("/", RULE_ID_SEPARATOR);
            ruleIdBuilder.append(prefix);
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                ruleIdBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append(RULE_ID_SEPARATOR);
            }
        } else if (StringUtils.isNotBlank(prefix)) {
            filter = new RuleFilter(null, prefix, null);
            prefix = prefix.replace("/", RULE_ID_SEPARATOR);
            ruleIdBuilder.append(prefix);
        } else if (tags != null && !tags.isEmpty()) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                ruleIdBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append(RULE_ID_SEPARATOR);
            }
            AndOperator andOperator = new AndOperator("", tags);
            filter = new RuleFilter(andOperator, null, null);
        } else {
            filter = new RuleFilter("");
        }
        ruleIdBuilder.append(suffix);
        String ruleId = ruleIdBuilder.toString();

        return new LifecycleRule(Status.ENABLED, null, expiration, filter, ruleId, null, null, null);
    }

    @Override
    public LifecycleConfiguration getBucketLifecycle() {
        try {
            return minioClient.getBucketLifecycle(
                    GetBucketLifecycleArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .build()
            );
        } catch (Exception e) {
            log.error("【MinIO】获取桶生命周期配置失败: {}", e.getMessage());
            throw new SystemException("获取桶生命周期配置失败: " + e.getMessage());
        }
    }


    @Override
    public String replaceCdn(String fileUrl) {
        if (StringUtils.isNotBlank(minioProperties.getCdn())) {
            return fileUrl.replace(minioProperties.getEndpoint(), minioProperties.getCdn());
        }
        return fileUrl;
    }

    @Override
    public String getFileUrlWithCdn(String objectName) {
        if (StringUtils.isNotBlank(minioProperties.getCdn())) {
            return buildFileUrl(minioProperties.getCdn(), objectName);
        }
        return getFileUrl(objectName);
    }

    private String buildFileUrl(String baseUrl, String objectName) {
        String normalizedBaseUrl = StringUtils.removeEnd(baseUrl, "/");
        return normalizedBaseUrl + "/" + minioProperties.getBucketName() + "/" + objectName;
    }


    @Override
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .build()
                );
                log.info("【MinIO】创建存储桶: {}", minioProperties.getBucketName());

                setBucketPublicRead();
            }
        } catch (Exception e) {
            log.error("【MinIO】检查/创建存储桶失败: {}", minioProperties.getBucketName(), e);
            throw new SystemException("检查/创建存储桶失败: " + e.getMessage());
        }
    }

    @Override
    public void setBucketPublicRead() {
        try {
            String policy = String.format("""
                    {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Effect": "Allow",
                                "Principal": {"AWS": "*"},
                                "Action": "s3:GetObject",
                                "Resource": "arn:aws:s3:::%s/*"
                            }
                        ]
                    }
                    """, minioProperties.getBucketName());

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .config(policy)
                            .build()
            );
            log.info("【MinIO】设置存储桶为公开读取: {}", minioProperties.getBucketName());
        } catch (Exception e) {
            log.warn("【MinIO】设置存储桶公开读取失败（不影响使用）: {}", minioProperties.getBucketName(), e);
        }
    }
}
