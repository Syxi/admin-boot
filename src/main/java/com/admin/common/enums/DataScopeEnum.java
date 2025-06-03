package com.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataScopeEnum implements IBaseEnum<Integer> {

    ALL(0, "全部数据权限"),
    ORGANIZATION_AND_CHILDREN(1, "组织及子部门数据权限"),
    ORGANIZATION(2, "本组织数据权限"),
    CREATE_USER(3, "本人数据权限");

    private final Integer value;
    private final String label;
}
