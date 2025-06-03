package com.admin.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class JacksonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // 设置输出时包含属性的访问类型（public/get/is）
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 忽略空Bean转json的错误
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * 对象转换成json字符串
     * @param obj 要转换的对象
     * @return JSON字符串
     */
    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            log.error("Failed to convert object to JSON string: {}" , obj);
            throw new RuntimeException("Failed to convert object to JSON string", e);
        }
    }

    /**
     * json字符串转换成对象
     * @param jsonString JSON字符串
     * @param clazz 要转换的目标类型
     * @param <T> 泛型标记
     * @return 转换后的对象
     */
    public static <T> T toObject(String jsonString, Class<T> clazz) {
        try {
            return mapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            log.error("Failed to convert JSON string to object: {}" , jsonString);
            throw new RuntimeException("Failed to convert JSON string to object", e);
        }
    }
}