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
    * 字典数据表
    */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_dict_value")
public class SysDictValue extends BaseEntity {

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;


    /**
     * 字典类型编码
     */
    @TableField(value = "type_code")
    private String typeCode;

    /**
     * 字典项名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 字典项值
     */
    @TableField(value = "value")
    private String value;

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