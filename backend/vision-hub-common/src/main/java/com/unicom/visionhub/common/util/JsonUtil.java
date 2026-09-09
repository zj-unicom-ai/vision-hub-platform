package com.unicom.visionhub.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;

public class JsonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);
    }

    @SneakyThrows
    public static JsonNode parse(String str) {
        return objectMapper.readValue(str, JsonNode.class);
    }

    @SneakyThrows
    public static String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }

    @SneakyThrows
    public static byte[] toBytes(Object obj) {
        return objectMapper.writeValueAsBytes(obj);
    }

    @SneakyThrows
    public static String toBase64(Object obj) {
        return Base64.encodeBase64String(objectMapper.writeValueAsBytes(obj));
    }

    @SneakyThrows
    public static <T> T fromJson(String payload, Class<T> tClass) {
        return objectMapper.readValue(payload, tClass);
    }

    @SneakyThrows
    public static <T> T fromJson(String payload, TypeReference<T> tTypeReference) {
        return objectMapper.readValue(payload, tTypeReference);
    }

    @SneakyThrows
    public static <T> T fromBase64(String payload, Class<T> tClass) {
        return objectMapper.readValue(Base64.decodeBase64(payload), tClass);
    }

    @SneakyThrows
    public static <T> T fromBytes(byte[] payload, Class<T> tClass) {
        return objectMapper.readValue(payload, tClass);
    }

    @SneakyThrows
    public static <T> T fromBase64(String payload, TypeReference<T> tTypeReference) {
        return objectMapper.readValue(Base64.decodeBase64(payload), tTypeReference);
    }

    public static <T> T convert(Object obj, Class<T> clazz) {
        return objectMapper.convertValue(obj, clazz);
    }

    public static <T> T convert(Object obj, TypeReference<T> tTypeReference) {
        return objectMapper.convertValue(obj, tTypeReference);
    }
}
