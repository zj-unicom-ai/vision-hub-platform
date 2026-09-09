package com.unicom.visionhub.common.exception;

import com.unicom.visionhub.common.enums.StatusCode;
import lombok.Getter;


@Getter
public class ServiceException extends RuntimeException {

    private final Integer code;

    public ServiceException(StatusCode statusCode) {
        super(statusCode.getMessage());
        this.code = statusCode.getCode();
    }

    public ServiceException(StatusCode statusCode, String message) {
        super(message);
        this.code = statusCode.getCode();
    }

    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public ServiceException(String message) {
        super(message);
        this.code = 500;
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }
}
