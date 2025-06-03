package com.admin.module.system.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 下拉选项对象
 * @Author: suYan
 * @Date: 2023-11-29
 */
@Data
@NoArgsConstructor
public class OptionVO<T> {

    // 选项的值
    private T value;

    // 选项的标签
    private String label;

    // 子选项列表
    private List<OptionVO> children;

    public OptionVO(T value, String label, List<OptionVO> children) {
        this.value = value;
        this.label = label;
        this.children = children;
    }


    public OptionVO(T value, String label) {
        this.value = value;
        this.label = label;
    }


}
