package com.admin.common.excel;

import com.admin.common.excel.listener.MyAnalysisEventListener;
import com.alibaba.excel.EasyExcel;

import java.io.InputStream;

/**
 * excel 工具类
 * @Author: suYan
 * @Date: 2024-01-22
 */

public class ExcelUtil {

    public static <T> ImportResult importExcel(InputStream inputStream, Class clazz, MyAnalysisEventListener<T> listener) {
        EasyExcel.read(inputStream, clazz, listener).sheet().doRead();
        return listener.getResult();
    }
}
