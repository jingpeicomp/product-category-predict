package com.jinpei.product.category.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * 工具类
 * Created by liuzhaoming on 2018/12/12.
 */
@Slf4j
public final class CategoryUtils {
    private CategoryUtils() {
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 将Json字符串转化为Java对象
     *
     * @param jsonStr json字符串
     * @param tClass  Java对象类
     * @param <T>     泛型
     * @return Java对象
     */
    public static <T> T fromJson(String jsonStr, Class<T> tClass) {
        try {
            return OBJECT_MAPPER.readValue(jsonStr, tClass);
        } catch (IOException e) {
            log.error("Fail convert json string to object {} , {}", jsonStr, tClass, e);
        }

        return null;
    }

    public static <T> T fromJson(String jsonStr, TypeReference<T> reference) {
        try {
            return OBJECT_MAPPER.readValue(jsonStr, reference);
        } catch (IOException e) {
            log.error("Fail convert json string to object {} , {}", jsonStr, reference, e);
        }

        return null;
    }

    /**
     * 将Java对象转化为JSON字符串
     *
     * @param obj obj对象
     * @return JSON字符串
     */
    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Fail convert object to json string {}", obj, e);
        }

        return "";
    }

    /**
     * 判断本地文件（目录）是否存在
     *
     * @param path 文件路径
     * @return boolean 存在为true， 反之false
     */
    public static boolean isFileExist(String path) {
        File file = new File(path);
        return file.exists();
    }
}
