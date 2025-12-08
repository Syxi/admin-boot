package com.admin.common.excel.importvo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import lombok.Data;

/**
 * 用户 excel导入
 * @Author: suYan
 * @Date: 2024-01-22
 */
@Data
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
public class UserImportVO {

    @ExcelProperty(value = "用户名")
    @ColumnWidth(20)
    private String username;

    @ExcelProperty(value = "真实姓名")
    @ColumnWidth(20)
    private String realName;

    @ExcelProperty(value = "性别")
    @ColumnWidth(15)
    private String gender;

    @ExcelProperty(value = "手机号码")
    @ColumnWidth(20)
    private String mobile;

    @ExcelProperty(value = "邮箱")
    @ColumnWidth(25)
    private String email;

    @ExcelProperty(value = "角色名称")
    @ColumnWidth(20)
    private String roleNames;

    @ExcelProperty(value = "部门编码")
    @ColumnWidth(20)
    private String deptCode;
}