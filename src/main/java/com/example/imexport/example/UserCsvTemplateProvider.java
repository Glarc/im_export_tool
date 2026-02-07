package com.example.imexport.example;

import com.example.imexport.core.CsvTemplateProvider;
import org.springframework.stereotype.Component;

/**
 * 用户 CSV 模板提供者示例
 * 演示如何实现 CSV 模板提供者
 */
@Component
public class UserCsvTemplateProvider implements CsvTemplateProvider<UserCsvModel> {

    @Override
    public String getBusinessType() {
        return "USER_CSV_IMPORT";
    }

    @Override
    public Class<UserCsvModel> getCsvModelClass() {
        return UserCsvModel.class;
    }

    @Override
    public String getTemplateFileName() {
        return "user_csv_import";
    }

    @Override
    public String[] getCsvHeaders() {
        return new String[]{"用户名", "邮箱", "手机号", "年龄", "部门"};
    }
}
