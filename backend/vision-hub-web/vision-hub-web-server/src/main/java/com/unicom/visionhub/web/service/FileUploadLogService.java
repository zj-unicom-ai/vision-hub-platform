package com.unicom.visionhub.web.service;

import com.unicom.visionhub.web.entity.model.FileUploadLog;
import org.springframework.web.multipart.MultipartFile;


public interface FileUploadLogService {


    Long logUploadSuccess(String businessType, MultipartFile file, String objectName,
                          String minioUrl, String bucketName, String ipAddress, String userAgent);


    Long logUploadFailure(String businessType, MultipartFile file, String errorMessage,
                          String ipAddress, String userAgent);


    Long save(FileUploadLog log);
}
