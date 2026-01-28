package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 在线用户表
 * @TableName t_online_user
 */
@TableName(value ="t_online_user")
@Data
public class OnlineUser {
    /**
     * 
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
     * 用户名
     */
    private String username;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
}