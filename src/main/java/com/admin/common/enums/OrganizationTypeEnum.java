package com.admin.common.enums;

import lombok.Getter;

/**
 * 组织类型： 1: 机构 ,  2：部门
 */
@Getter
public enum OrganizationTypeEnum implements IBaseEnum<Integer> {

    ORGANIZATION(1, "机构"),

    DEPT(2, "部门");


    private final Integer value;

    private final String label;

    private OrganizationTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
