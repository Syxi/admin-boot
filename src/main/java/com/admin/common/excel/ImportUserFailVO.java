package com.admin.common.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 批量导入用户检测结果类
 */
@Data
public class ImportUserFailVO {

    @ExcelProperty("行号")
    private int rowNum;

    @ExcelProperty("校验信息")
    private String msg;

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("真实姓名")
    private String realName;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("手机")
    private String mobile;

    @ExcelProperty("角色名称")
    private String roleNames;

    @ExcelProperty("机构名称")
    private String organName;

    @ExcelProperty("部门名称")
    private String deptName;

}
