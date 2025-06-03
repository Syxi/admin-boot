package com.admin.module.system.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章分类下拉选项
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CategoryOption {

    private Long id;

    private String value;

    private String label;

}
