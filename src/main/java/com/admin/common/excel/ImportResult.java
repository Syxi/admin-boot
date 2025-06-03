package com.admin.common.excel;

import lombok.Data;

@Data
public class ImportResult {

    // 有效条数
    private int validCount;

    // 无效条数
    private int invalidCount;
}
