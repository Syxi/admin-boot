package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public SysUserDept(Long userId, Long deptId) {
    }
}