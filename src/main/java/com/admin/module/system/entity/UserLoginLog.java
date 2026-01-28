package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 * @TableName user_login_log
 */
@TableName(value ="user_login_log")
@Data
public class UserLoginLog implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 租户ID
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Long tenantId;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录ip
     */
    private String ip;

    /**
     * 登录地址
     */
    private String address;

    /**
     * 登录的操作系统
     */
    private String os;

    /**
     * 登录的浏览器
     */
    private String browser;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}