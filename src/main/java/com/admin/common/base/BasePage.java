package com.admin.common.base;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author suYan
 * @date 2023/4/4 15:16
 */
@Data
public class BasePage {

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页记录数")
    private Integer limit = 10;

}
