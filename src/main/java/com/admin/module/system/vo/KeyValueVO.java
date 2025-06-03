package com.admin.module.system.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 路由参数
 */
@Data
@NoArgsConstructor
public class KeyValueVO {

    /**
     * 选项的标签
     */
    private String key;

    /**
     * 选项的值
     */
    private String value;

    public KeyValueVO(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
