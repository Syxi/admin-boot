package com.admin.module.system.form;

import lombok.Data;

@Data
public class PositionForm {

    private Long positionId;

    /**
     * 岗位名称
     */
    private String positionName;

    /**
     * 所属组织部门
     */
    private Long deptId;

    /**
     * 岗位描述
     */
    private String description;

    /**
     * 状态 (1: 正常，-1：禁用)
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 薪资范围
     */
    private String salaryRange;

    /**
     * 工作经验
     */
    private String experience;

    /**
     * 教育背景 (0: 无，1: 专科，2：本科，3: 研究生，4: 博士，5：硕士)
     */
    private Integer education;

}
