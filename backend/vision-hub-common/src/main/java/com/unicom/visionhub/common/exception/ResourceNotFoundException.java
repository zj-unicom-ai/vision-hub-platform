package com.unicom.visionhub.common.exception;

import com.unicom.visionhub.common.enums.StatusCode;


public class ResourceNotFoundException extends ServiceException {

    public ResourceNotFoundException(StatusCode statusCode) {
        super(statusCode);
    }

    public ResourceNotFoundException(String message) {
        super(StatusCode.NOT_FOUND.getCode(), message);
    }

    public ResourceNotFoundException(String resourceName, Long resourceId) {
        super(StatusCode.NOT_FOUND.getCode(), String.format("%s [id=%d] 不存在", resourceName, resourceId));
    }
}
