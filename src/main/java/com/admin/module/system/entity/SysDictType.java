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
    * 字典类型表
    */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_dict_type")
public class SysDictType extends BaseEntity {

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;


    /**
     * 编码类型
     */
    @TableField(value = "type_code")
    private String typeCode;

    /**
     * 编码类型名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 状态((1:正常;-1:禁用))
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
}