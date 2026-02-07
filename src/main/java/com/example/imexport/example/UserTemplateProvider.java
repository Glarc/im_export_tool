package com.example.imexport.example;

import com.example.imexport.core.ExcelTemplateProvider;
import org.springframework.stereotype.Component;

/**
 * 示例业务 - 用户导入模板提供者
 * 展示如何实现 ExcelTemplateProvider 接口
 */
@Component
public class UserTemplateProvider implements ExcelTemplateProvider<UserImportModel> {

    @Override
    public String getBusinessType() {
        return "USER_IMPORT";
    }

    @Override
    public Class<UserImportModel> getExcelModelClass() {
        return UserImportModel.class;
    }

    @Override
    public String getTemplateFileName() {
        return "user_import";
    }
}
