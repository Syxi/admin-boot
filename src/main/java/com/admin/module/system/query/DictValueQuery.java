package com.admin.module.system.query;

import com.admin.common.base.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author suYan
 * @date 2023/4/4 15:21
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典项查询对象")
@Data
public class DictValueQuery extends BasePage {

    @Schema(description = "字典项名称")
    private String name;

    @Schema(description = "字典类型编码")
    private String typeCode;

}
