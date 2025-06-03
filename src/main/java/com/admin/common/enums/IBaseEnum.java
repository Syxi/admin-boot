package com.admin.common.enums;

import org.springframework.util.ObjectUtils;

import java.util.EnumSet;
import java.util.Objects;

/**
 * 枚举通用接口
 * @Author: suYan
 * @Date: 2024-01-22
 */


public interface IBaseEnum<T> {

    T getValue();

    String getLabel();


    /**
     * 根据值获取枚举
     * @param value
     * @param clazz
     * @return
     * @param <E>
     */
    static <E extends Enum<E> & IBaseEnum> E getEnumByValue(Object value, Class<E> clazz) {
        Objects.requireNonNull(value);
        // 获取枚举类型下的所有枚举
        EnumSet<E> allEnums = EnumSet.allOf(clazz);
        E matchEnum = allEnums.stream()
                .filter(e -> ObjectUtils.nullSafeEquals(e.getValue(), value))
                .findFirst()
                .orElse(null);
        return matchEnum;
    }


    /**
     * 根据值获取文本标签
     *
     * @param value
     * @param clazz
     * @param <E>
     * @return
     */
    static <E extends Enum<E> & IBaseEnum> String getLabelByValue(Object value, Class<E> clazz) {
        Objects.requireNonNull(value);
        EnumSet<E> allEnums = EnumSet.allOf(clazz); // 获取类型下的所有枚举
        E matchEnum = allEnums.stream()
                .filter(e -> ObjectUtils.nullSafeEquals(e.getValue(), value))
                .findFirst()
                .orElse(null);

        String label = null;
        if (matchEnum != null) {
            label = matchEnum.getLabel();
        }
        return label;
    }


    /**
     * 根据文本标签获取值
     *
     * @param label
     * @param clazz
     * @param <E>
     * @return
     */
    static <E extends Enum<E> & IBaseEnum> Object getValueByLabel(String label, Class<E> clazz) {
        Objects.requireNonNull(label);
        EnumSet<E> allEnums = EnumSet.allOf(clazz); // 获取类型下的所有枚举
        String finalLabel = label;
        E matchEnum = allEnums.stream()
                .filter(e -> ObjectUtils.nullSafeEquals(e.getLabel(), finalLabel))
                .findFirst()
                .orElse(null);

        Object value = null;
        if (matchEnum != null) {
            value = matchEnum.getValue();
        }
        return value;
    }

}
