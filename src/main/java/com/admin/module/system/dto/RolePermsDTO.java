package com.admin.module.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 角色权限业务对象
 * @Author: suYan
 * @Date: 2023-12-14
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RolePermsDTO {

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 权限标识集合
     */
    private Set<String> perms;


}
