package com.admin.module.system.dto;

import lombok.Data;

import java.util.Set;

/**
 * 用户认证信息
 * @author suYan
 * @date 2023/9/23 18:40
 */
@Data
public class UserAuthInfo {

    private Long userId;

    private String username;

    private String password;

    private String realName;

    private Integer status;

    // 组织id
    private Long organId;

    // 数据权限范围
    private Integer dataScope;

    // 角色编码
    private Set<String> roles;

    // 权限标识
    private Set<String> permissions;



}
