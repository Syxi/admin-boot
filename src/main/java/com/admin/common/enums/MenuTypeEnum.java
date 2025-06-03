package com.admin.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 菜单类型枚举类
 * @author suYan
 * @date 2023/4/1 13:34
 */
@Getter
public enum MenuTypeEnum  implements IBaseEnum<Integer> {

    NULL(0, null),

    MENU(1, "菜单"),

    CATALOG(2, "目录"),

    EXLINK(3, "外链"),

    BUTTON(4, "按钮");

    @EnumValue // Mybatisplus 提供注解表示插入数据库时插入该值
    private Integer value;

    private String label;

    MenuTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
