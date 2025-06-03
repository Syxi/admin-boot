package com.admin.common.enums;

import lombok.Getter;

/**
 * 性別枚举类
 * @author suYan
 * @date 2023/4/1 14:14
 */

public enum GenderEnum implements IBaseEnum<Integer>  {

    MALE(1, "男"),
    FEMALE(2, "女"),
    UNKNOWN(3, "未知");

    @Getter
    private Integer value;

    @Getter
    private  String label;

    GenderEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
