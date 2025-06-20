package com.admin.module.system.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author suYan
 * @date 2023/4/2 15:13
 */
@Schema(description = "用户对象")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserForm {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "昵称")
    private String realName;

    @Pattern(regexp = "^1(3\\d|4[5-9]|5[0-35-9]|6[2567]|7[0-8]|8\\d|9[0-35-9])\\d{8}$", message = "{mobile.valid}")
    private String mobile;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "用户状态((1:正常;-1:禁用))")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "部门名称")
    private Long deptId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "角色ID集合")
    private List<Long> roleIds;




}
