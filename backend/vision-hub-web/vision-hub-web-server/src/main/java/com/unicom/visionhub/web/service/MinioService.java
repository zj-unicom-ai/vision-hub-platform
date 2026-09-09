package com.unicom.visionhub.web.service;

import com.unicom.visionhub.web.dto.FileMetadata;
import com.unicom.visionhub.web.dto.MinioResp;
import io.minio.messages.LifecycleConfiguration;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;


public interface MinioService {


    String uploadFile(MultipartFile file);


    String uploadFile(MultipartFile file, String objectName);


    String uploadFile(InputStream inputStream, String objectName, long size, String contentType);


    MinioResp uploadFileWithResp(MultipartFile file, String objectName);


    String uploadBytes(String objectName, byte[] body, Map<String, String> tags);


    String uploadFile(String objectName, File file);


    InputStream downloadFile(String objectName);


    byte[] downloadAsBytes(String objectName);


    void deleteFile(String objectName);


    void remove(String bucketName, String objectName);


    String getFileUrl(String objectName);


    String getPresignedUrl(String objectName, Integer expires);


    String getPresignedUrl(String objectName);


    boolean fileExists(String objectName);


    FileMetadata getFileMetadata(String objectName);


    void updateTags(String objectName, Map<String, String> tags);


    Map<String, String> getFileTags(String objectName);


    void setFileExpiry(Integer expireDays, String prefix, Map<String, String> tags);


    void setFileExpiry(Date expireDate, String prefix, Map<String, String> tags);


    LifecycleConfiguration getBucketLifecycle();


    String replaceCdn(String fileUrl);


    String getFileUrlWithCdn(String objectName);


    void ensureBucketExists();


    void setBucketPublicRead();
}
