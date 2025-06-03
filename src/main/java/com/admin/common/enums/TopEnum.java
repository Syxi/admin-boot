package com.admin.common.enums;

import lombok.Getter;

@Getter
public enum TopEnum implements IBaseEnum<Integer> {

    IS_TOP(1, "置顶"),

    NO_TOP(-1, "取消置顶");


    private final Integer value;

    private final String label;

    TopEnum(int name, String label) {
        this.value = name;
        this.label = label;
    }

}
