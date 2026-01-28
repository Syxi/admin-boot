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
    * 用户信息表
 * @author suyan
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_user")
public class SysUser extends BaseEntity {

    @TableId(value = "user_id")
    private Long userId;

    /**
     * 部门ID
     */
    private Long deptId;


    /**
     * 用户名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 密码
     */
    @TableField(value = "password")
    private String password;

    /**
     * 昵称
     */
    @TableField(value = "real_name")
    private String realName;


    /**
     * 性别((1:男;2:女))
     */
    @TableField(value = "gender")
    private Integer gender;


    /**
     * 用户头像
     */

    @TableField(value = "avatar")
    private String avatar;

    /**
     * 联系方式
     */
    @TableField(value = "mobile")
    private String mobile;

    /**
     * 用户状态(1:正常;-1:禁用)
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 用户邮箱
     */
    @TableField(value = "email")
    private String email;


    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;






}