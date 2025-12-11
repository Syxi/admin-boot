package com.admin.common.enums;

import lombok.Getter;

@Getter
public enum DataScopeEnum implements IBaseEnum<Integer> {

    ALL(0, "全部数据权限"),
    DEPT_AND_CHILDREN(1, "组织及子部门数据权限"),
    DEPT(2, "本组织数据权限"),
    CREATE_USER(3, "本人数据权限");

    private final Integer value;
    private final String label;

    DataScopeEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static DataScopeEnum fromValue(Integer value) {
        if (value == null) return null;
        for (DataScopeEnum e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        return null;
    }
}