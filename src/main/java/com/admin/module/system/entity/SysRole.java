package com.admin.module.system.entity;

import com.admin.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
    * 角色表
    */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_role")
public class SysRole extends BaseEntity {

    @TableId(value = "role_id")
    private Long roleId;


    /**
     * 角色名称
     */
    @TableField(value = "role_name")
    private String roleName;

    /**
     * 角色编码
     */
    @TableField(value = "role_code")
    private String roleCode;

    /**
     * 显示顺序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 角色状态((1:正常;-1:禁用))
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 数据权限(0-所有数据权限；1-组织及子部门数据权限；2-本部门数据权限；3-本人数据权限)
     */
    @TableField(value = "data_scope")
    private Integer dataScope;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;







}