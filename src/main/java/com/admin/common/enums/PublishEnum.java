package com.admin.common.enums;

import lombok.Getter;


public enum PublishEnum implements IBaseEnum<Integer> {
    IS_PUBLISH(1, "发布"),

    NO_PUBLISH(-1, "未发布");

    @Getter
    private  Integer value;

    @Getter
    private  String label;


    PublishEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

}
