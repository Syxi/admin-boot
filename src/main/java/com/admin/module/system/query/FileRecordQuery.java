package com.admin.module.system.query;

import com.admin.common.base.BasePage;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件查询对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FileRecordQuery extends BasePage {

    private String fileName;
}
