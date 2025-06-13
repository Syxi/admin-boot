package com.admin.module.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DeptVO {

    @Schema(description = "部门id")
    private Long id;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "部门编码")
    private String deptCode;

    @Schema(description = "部门类型 (1：机构，2：部门）")
    private Integer deptType;

    @Schema(description = "父节点id")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Schema(description = "部门图标")
    private String deptImg;

    @Schema(description = "部门简介")
    private String deptIntroduction;


    @Schema(description = "部门联系电话")
    private String deptPhone;


    @Schema(description = "部门地址")
    private String deptAddress;


    @Schema(description = "备注")
    private String remark;


    @Schema(description = "部门子列表")
    private List<DeptVO> children;











}
