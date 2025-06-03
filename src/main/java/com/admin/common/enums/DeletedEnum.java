package com.admin.common.enums;

import lombok.Getter;

/**
 * 逻辑删除枚举类
 * @author suYan
 * @date 2024/1/14 10:41
 */
public enum DeletedEnum implements IBaseEnum<Integer> {

    IS_DELETE(1, "已删除"),

    NO_DELETE(0, "未删除");

    @Getter
    private Integer value;

    @Getter
    private String label;


    DeletedEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
