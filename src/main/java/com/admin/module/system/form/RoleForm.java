package com.admin.module.system.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author suYan
 * @date 2023/4/4 16:04
 */
@Schema(description = "角色表单对象")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RoleForm {

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "数据权限范围")
    private Integer dataScope;

    @Schema(description = "角色状态(1:正常；-1:停用)")
    private Integer status;

    @Schema(description = "角色创建时间")
    private LocalDateTime createTime;

    @Schema(description = "角色更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "备注")
    private String remark;






}
