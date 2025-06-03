package com.admin.module.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author suYan
 * @date 2023/4/4 14:25
 */
@Schema(description = "角色分页视图对象")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RoleVO {

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色状态")
    private Integer status;

    @Schema(description = "数据权限范围")
    private Integer dataScope;

    @Schema(description = "排序")
    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime  updateTime;





}
