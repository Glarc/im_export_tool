package com.example.imexport.example;

import lombok.Data;

/**
 * 用户 CSV 导入数据模型
 * 演示如何定义 CSV 数据模型
 */
@Data
public class UserCsvModel {
    private String username;
    private String email;
    private String phone;
    private Integer age;
    private String department;
}
