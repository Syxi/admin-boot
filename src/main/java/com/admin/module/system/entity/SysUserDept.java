package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 * @TableName sys_user_dept
 */
@TableName(value ="sys_user_dept")
@Data
public class SysUserDept implements Serializable {
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
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}