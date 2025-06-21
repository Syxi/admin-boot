package com.admin.common.excel.export;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * excel 导出用户
 * @Author: suYan
 * @Date: 2024-01-22
 */
@Data
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER) // 导出内容居中
public class UserExportVO {

    @ExcelIgnore
    private Long userId;

    @ExcelProperty(value = "用户名")
    private String username;

    @ExcelProperty(value = "真实姓名")
    @ColumnWidth(20)
    private String realName;

    @ExcelProperty(value = "性别")
    private String gender;

    @ExcelProperty(value = "手机号码")
    @ColumnWidth(20)
    private String mobile;

    @ExcelProperty(value = "邮箱")
    @ColumnWidth(20)
    private String email;

    @ExcelProperty(value = "角色名称")
    @ColumnWidth(40)
    private String roleNames;

    @ExcelProperty(value = "部门名称")
    @ColumnWidth(20)
    private String deptName;

    @ExcelProperty(value = "创建时间")
    @DateTimeFormat("yyyy-MM-dd")
    @ColumnWidth(20)
    private LocalDateTime createTime;
}
