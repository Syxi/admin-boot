package com.admin.common.enums;

import lombok.Getter;

/**
 * 状态枚举
 * @author suYan
 * @date 2023/4/1 13:48
 */

public enum StatusEnum implements IBaseEnum<Integer> {

    ENABLE(1, "启用"),

    DISABLE(-1, "禁用");

    @Getter
    private  Integer value;

    @Getter
    private  String label;

    StatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

}
