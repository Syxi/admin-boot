package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 岗位和组织关联表
 * @TableName sys_position_dept
 */
@TableName(value ="sys_position_dept")
@Data
public class SysPositionDept implements Serializable {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 岗位id
     */
    private Long positionId;

    /**
     * 组织id
     */
    private Long deptId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public SysPositionDept(Long positionId, Long deptId) {
        this.positionId = positionId;
        this.deptId = deptId;
    }
}