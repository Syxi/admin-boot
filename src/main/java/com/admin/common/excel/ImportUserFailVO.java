package com.admin.common.excel;

import lombok.Data;

@Data
public class ImportUserFailVO {

    /**
     * 行号
     */
    private Integer rowNum;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色名称
     */
    private String roleNames;

    /**
     * 机构名称
     */
    private String orgName;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 错误信息
     */
    private String msg;
}