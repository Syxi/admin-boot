package com.admin.module.system.query;

import com.admin.common.base.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author suYan
 * @date 2023/4/4 14:56
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户查询对象")
@Data
public class UserQuery extends BasePage {

    @Schema(description = "用户账号")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "部门id")
    private Long organId;

    @Schema(description = "用户状态(1:正常;-1:禁用)")
    private Integer status;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String endTime;


}
