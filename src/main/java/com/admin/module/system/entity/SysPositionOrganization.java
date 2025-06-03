package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 岗位和组织关联表
 * @TableName sys_position_organization
 */
@TableName(value ="sys_position_organization")
@Data
public class SysPositionOrganization implements Serializable {
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
    private Long organId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public SysPositionOrganization(Long positionId, Long organId) {
        this.positionId = positionId;
        this.organId = organId;
    }
}