package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户和部门关联表
 * @TableName sys_user_dept
 */
@TableName(value ="sys_user_dept")
@Data
public class SysUserDept {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 角色id
     */
    private Long userId;

    /**
     * 组织id
     */
    private Long deptId;

    /**
     * 租户ID
     */
    private Long tenantId;

    @TableField(fill = FieldFill.INSERT)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private LocalDateTime updateTime;


    public SysUserDept(Long userId, Long deptId) {
        this.userId = userId;
        this.deptId = deptId;
    }
}