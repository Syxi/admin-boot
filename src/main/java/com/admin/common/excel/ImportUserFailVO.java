package com.admin.common.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import lombok.Data;

/**
 * 批量导入用户检测结果类
 */
@Data
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
public class ImportUserFailVO {

    @ExcelProperty("行号")
    @ColumnWidth(10)
    private int rowNum;

    @ExcelProperty("校验信息")
    @ColumnWidth(30)
    private String msg;

    @ExcelProperty("用户名")
    @ColumnWidth(20)
    private String username;

    @ExcelProperty("真实姓名")
    @ColumnWidth(20)
    private String realName;

    @ExcelProperty("邮箱")
    @ColumnWidth(25)
    private String email;

    @ExcelProperty("手机")
    @ColumnWidth(20)
    private String mobile;

    @ExcelProperty("角色名称")
    @ColumnWidth(20)
    private String roleNames;

    @ExcelProperty("部门编码")
    @ColumnWidth(20)
    private String deptName;
}