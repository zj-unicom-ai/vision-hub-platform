package com.unicom.visionhub.common.exception;

import lombok.Getter;


@Getter
public class SystemException extends RuntimeException {

    private final Integer code;

    public SystemException(String message) {
        super(message);
        this.code = 500;
    }

    public SystemException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public SystemException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
