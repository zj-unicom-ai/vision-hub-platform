package com.unicom.visionhub.web.validation;

import org.springframework.web.multipart.MultipartFile;


public class FileUploadUtil {


    private static final String[] ALLOWED_IMAGE_FORMATS = {"jpg", "jpeg", "png"};


    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;


    public static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            String sizeInMB = String.format("%.1f", MAX_FILE_SIZE / (1024.0 * 1024.0));
            throw new IllegalArgumentException("图片大小不能超过" + sizeInMB + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String fileExtension = getFileExtension(originalFilename);
        if (!isAllowedImageFormat(fileExtension)) {
            String allowedFormats = String.join(", ", ALLOWED_IMAGE_FORMATS);
            throw new IllegalArgumentException("只支持以下图片格式：" + allowedFormats);
        }
    }


    public static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }


    private static boolean isAllowedImageFormat(String extension) {
        for (String allowedFormat : ALLOWED_IMAGE_FORMATS) {
            if (allowedFormat.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}
