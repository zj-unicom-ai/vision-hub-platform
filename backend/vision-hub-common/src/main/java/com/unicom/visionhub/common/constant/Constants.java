package com.unicom.visionhub.common.constant;


public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Constant class cannot be instantiated");
    }


    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 500;
    public static final int NOT_FOUND_CODE = 404;
    public static final int BAD_REQUEST_CODE = 400;
    public static final int UNAUTHORIZED_CODE = 401;
    public static final int FORBIDDEN_CODE = 403;


    public static final Integer STATUS_ENABLED = 1;
    public static final Integer STATUS_DISABLED = 0;
    public static final Integer STATUS_DELETED = -1;


    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";


    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int MAX_PAGE_SIZE = 100;


    public static final String UTF8 = "UTF-8";
    public static final String COMMA = ",";
    public static final String EMPTY_STRING = "";
    public static final String SLASH = "/";


    public static final String MODEL_TYPE_QWEN = "qwen";
    public static final String MODEL_TYPE_DEEPSEEK = "deepseek";
    public static final String MODEL_TYPE_LOCAL = "local";
    public static final String MODEL_TYPE_OPENAI = "openai";


    public static final String ALGORITHM_TYPE_LLM = "llm";
    public static final String ALGORITHM_TYPE_VISION = "vision";


    public static final String MODEL_FORMAT_YOLO = "yolo";
    public static final String MODEL_FORMAT_ONNX = "onnx";
    public static final String MODEL_FORMAT_TENSORRT = "tensorrt";
}
