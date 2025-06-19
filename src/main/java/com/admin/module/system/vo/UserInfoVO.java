package com.admin.module.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    @Schema(description = "用户性别")
    private Integer gender;

    @Schema(description = "手机号码")
    private String mobile;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "用户头像地址")
    private String avatar;

    @Schema(description = "用户角色编码集合")
    private Set<String> roles;

    @Schema(description = "用户权限标识集合")
    private Set<String> perms;

    @Schema(description = "用户职位名称集合")
    private String positions;

    @Schema(description = "用户部门名称集合")
    private String deptNames;

    @Schema(description = "用户角色名称集合")
    private String roleNames;

    @Schema(description = "用户最后登录时间")
    private LocalDateTime  lastLoginTime;
}
