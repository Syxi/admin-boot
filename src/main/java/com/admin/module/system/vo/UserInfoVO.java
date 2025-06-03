package com.admin.module.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 用户登录成功后，获取的用户信息
 * @Author: suYan
 * @Date: 2023-11-28
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserInfoVO {

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String realName;

    @Schema(description = "用户头像地址")
    private String avatar;

    @Schema(description = "用户角色编码集合")
    private Set<String> roles;

    @Schema(description = "用户权限标识集合")
    private Set<String> perms;
}
