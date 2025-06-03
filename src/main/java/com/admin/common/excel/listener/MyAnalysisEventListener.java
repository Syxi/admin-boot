package com.admin.common.excel.listener;

import com.admin.common.excel.ImportResult;
import com.alibaba.excel.event.AnalysisEventListener;

/**
 * easyExcel 的自定义解决结果监听器
 * @Author: suYan
 * @Date: 2024-01-22
 */

public abstract class MyAnalysisEventListener<T> extends AnalysisEventListener<T> {

    /**
     *
     * @return 批量导入结果
     */
    public abstract ImportResult getResult();
}
