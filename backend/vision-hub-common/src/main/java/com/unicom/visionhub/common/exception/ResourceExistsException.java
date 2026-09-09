package com.unicom.visionhub.common.exception;

import com.unicom.visionhub.common.enums.StatusCode;


public class ResourceExistsException extends ServiceException {

    public ResourceExistsException(String message) {
        super(StatusCode.RESOURCE_EXISTS.getCode(), message);
    }

    public ResourceExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(StatusCode.RESOURCE_EXISTS.getCode(),
                String.format("%s [%s=%s] 已存在", resourceName, fieldName, fieldValue));
    }
}
