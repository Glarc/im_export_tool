package com.example.imexport.example;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 示例业务 - 用户表导入数据模型
 */
@Data
public class UserImportModel {

    @ExcelProperty(value = "用户名", index = 0)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ExcelProperty(value = "邮箱", index = 1)
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @ExcelProperty(value = "手机号", index = 2)
    private String phone;

    @ExcelProperty(value = "年龄", index = 3)
    private Integer age;

    @ExcelProperty(value = "部门", index = 4)
    private String department;
}
