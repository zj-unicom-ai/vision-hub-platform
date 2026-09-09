package com.unicom.visionhub.web.service.impl;

import com.unicom.visionhub.common.exception.SystemException;
import com.unicom.visionhub.web.entity.model.FileUploadLog;
import com.unicom.visionhub.web.mapper.FileUploadLogMapper;
import com.unicom.visionhub.web.service.FileUploadLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadLogServiceImpl implements FileUploadLogService {

    private final FileUploadLogMapper fileUploadLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long logUploadSuccess(String businessType, MultipartFile file, String objectName,
                                  String minioUrl, String bucketName, String ipAddress, String userAgent) {
        try {
            FileUploadLog uploadLog = buildLogEntity(businessType, file, ipAddress, userAgent);
            uploadLog.setObjectName(objectName);
            uploadLog.setMinioUrl(minioUrl);
            uploadLog.setBucketName(bucketName);
            uploadLog.setUploadStatus(1);

            fileUploadLogMapper.insert(uploadLog);
            log.info("【文件上传日志】记录上传成功: businessType={}, objectName={}", businessType, objectName);
            return uploadLog.getId();
        } catch (Exception e) {
            log.error("【文件上传日志】记录上传成功日志失败: businessType={}", businessType, e);
            throw new SystemException("记录上传日志失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long logUploadFailure(String businessType, MultipartFile file, String errorMessage,
                                  String ipAddress, String userAgent) {
        try {
            FileUploadLog uploadLog = buildLogEntity(businessType, file, ipAddress, userAgent);
            uploadLog.setUploadStatus(0);
            uploadLog.setErrorMessage(errorMessage);

            fileUploadLogMapper.insert(uploadLog);
            log.info("【文件上传日志】记录上传失败: businessType={}, error={}", businessType, errorMessage);
            return uploadLog.getId();
        } catch (Exception e) {
            log.error("【文件上传日志】记录上传失败日志失败: businessType={}", businessType, e);
            throw new SystemException("记录上传日志失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(FileUploadLog uploadLog) {
        try {
            fileUploadLogMapper.insert(uploadLog);
            return uploadLog.getId();
        } catch (Exception e) {
            log.error("【文件上传日志】保存日志失败", e);
            throw new SystemException("保存上传日志失败");
        }
    }


    private FileUploadLog buildLogEntity(String businessType, MultipartFile file,
                                          String ipAddress, String userAgent) {
        FileUploadLog log = new FileUploadLog();
        log.setBusinessType(businessType);
        log.setFileName(file.getOriginalFilename());
        log.setFileSize(file.getSize());
        log.setFileType(file.getContentType());

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            log.setFileExtension(originalFilename.substring(originalFilename.lastIndexOf(".") + 1));
        }

        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        return log;
    }
}
