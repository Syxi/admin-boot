package com.admin.module.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户分页视图对象
 * @author suYan
 * @date 2023/4/2 13:36
 */
@Schema(description = "用户分页对象")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserVO {

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String realName;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "性别")
    private String genderLabel;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "用户状态(1:正常;-1:禁用)")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "部门名称")
    private String organName;

    @Schema(description = "角色名称，多个用英文逗号(,)分隔")
    private String roleNames;


    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
