package com.admin.module.system.query;

import com.admin.common.base.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author suYan
 * @date 2023/4/4 15:05
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色查询对象")
@Data
public class RoleQuery extends BasePage {

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "升序或降序")
    private String ascOrDesc;

    @Schema(description = "排序字段")
    private String orderByColumn;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
