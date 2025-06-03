package com.admin.common.enums;

import lombok.Getter;

/**
 * 文件转换成pdf枚举类
 */
@Getter
public enum FileConvertEnum {

    UNCONVERTED(0, "未转化"),

    SUCCESS(1, "转换成功"),

    FAIL(-1, "转换失败");


    private Integer value;

    private String label;

    FileConvertEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
