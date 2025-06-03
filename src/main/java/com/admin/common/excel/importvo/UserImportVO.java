package com.admin.common.excel.importvo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 用户 excel导入
 * @Author: suYan
 * @Date: 2024-01-22
 */
@Data
public class UserImportVO {

    @ExcelProperty(value = "用户名")
    private String username;

    @ExcelProperty(value = "真实姓名")
    private String realName;

    @ExcelProperty(value = "性别")
    private String gender;

    @ExcelProperty(value = "手机号码")
    private String mobile;

    @ExcelProperty(value = "邮箱")
    private String email;

    @ExcelProperty(value = "角色名称")
    private String roleNames;

    @ExcelProperty(value = "机构名称")
    private String organName;

    @ExcelProperty(value = "部门名称")
    private String deptName;




}
