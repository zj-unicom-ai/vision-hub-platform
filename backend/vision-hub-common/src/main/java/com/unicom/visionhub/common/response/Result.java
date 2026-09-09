package com.unicom.visionhub.common.response;

import com.unicom.visionhub.common.enums.StatusCode;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;


@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    private Integer code;


    private String message;


    private T data;


    private LocalDateTime timestamp;


    public static <T> Result<T> success() {
        return success(null);
    }


    public static <T> Result<T> success(T data) {
        return success(StatusCode.SUCCESS.getMessage(), data);
    }


    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(StatusCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }


    public static <T> Result<T> error() {
        return error(StatusCode.ERROR.getMessage());
    }


    public static <T> Result<T> error(String message) {
        return error(StatusCode.ERROR.getCode(), message);
    }


    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }


    public static <T> Result<T> error(StatusCode statusCode) {
        return error(statusCode.getCode(), statusCode.getMessage());
    }


    public static <T> Result<T> error(StatusCode statusCode, String message) {
        return error(statusCode.getCode(), message);
    }


    public boolean isSuccess() {
        return StatusCode.SUCCESS.getCode().equals(this.code);
    }
}
