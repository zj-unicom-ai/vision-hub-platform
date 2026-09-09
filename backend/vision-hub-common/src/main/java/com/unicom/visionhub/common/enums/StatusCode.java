package com.unicom.visionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum StatusCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    RESOURCE_EXISTS(409, "资源已存在"),

    MODEL_NOT_FOUND(1001, "大模型不存在"),
    MODEL_EXISTS(1002, "大模型已存在"),
    MODEL_CALL_FAILED(1003, "大模型调用失败"),
    MODEL_IN_USE_BY_ALGORITHM(1004, "大模型被算法引用，无法删除"),
    MODEL_DISABLED_IN_USE(1005, "大模型被算法引用，无法停用"),

    PROMPT_NOT_FOUND(1101, "提示词模板不存在"),
    PROMPT_EXISTS(1102, "提示词模板已存在"),
    VARIABLE_PARSE_FAILED(1103, "变量解析失败"),

    ALGORITHM_NOT_FOUND(1201, "算法不存在"),
    ALGORITHM_EXISTS(1202, "算法已存在"),
    ALGORITHM_EXEC_FAILED(1203, "算法执行失败"),
    ALGORITHM_DISABLED(1204, "算法未上架"),
    ALGORITHM_IN_USE_BY_TASK(1205, "算法被任务引用，无法删除"),
    ALGORITHM_DISABLED_IN_USE(1206, "算法被任务引用，无法停用"),

    INDUSTRY_NOT_FOUND(1301, "行业不存在"),
    INDUSTRY_EXISTS(1302, "行业已存在"),

    DEVICE_NOT_FOUND(1501, "设备不存在"),
    DEVICE_EXISTS(1502, "设备已存在"),
    DEVICE_STREAM_INVALID(1503, "视频流地址无效"),
    DEVICE_IN_USE_BY_TASK(1504, "设备被任务引用，无法删除"),

    TASK_NOT_FOUND(1601, "任务不存在"),
    TASK_NAME_EXISTS(1602, "任务名称已存在"),
    TASK_DEVICE_NOT_FOUND(1603, "任务点位不存在"),
    TASK_STATUS_INVALID(1607, "任务状态流转非法"),
    TASK_SCHEDULE_OVERLAP(1608, "调度规则时间段存在重叠"),
    TASK_ALGORITHM_REQUIRED(1609, "任务必须绑定算法"),

    EVENT_NOT_FOUND(1701, "事件不存在"),
    EVENT_STATUS_INVALID(1702, "事件状态非法"),
    EVENT_NAME_REQUIRED(1703, "事件名称不能为空"),

    USER_NOT_FOUND(2001, "用户不存在"),
    USER_EXISTS(2002, "用户已存在"),
    USER_DISABLED(2003, "用户已禁用"),
    USERNAME_PASSWORD_ERROR(2004, "用户名或密码错误"),
    PASSWORD_ERROR(2005, "旧密码错误"),
    PASSWORD_SAME(2006, "新密码不能与旧密码相同"),
    CAPTCHA_INVALID(2007, "验证码错误或已过期"),

    FILE_NOT_FOUND(5001, "文件不存在"),
    FILE_UPLOAD_FAILED(5002, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(5003, "文件类型不支持");

    private final Integer code;
    private final String message;
}
