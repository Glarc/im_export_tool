package com.example.imexport.example;

import com.example.imexport.core.ExcelExportProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 示例业务 - 用户导出提供者
 * 展示如何实现 ExcelExportProvider 接口
 */
@Component
public class UserExportProvider implements ExcelExportProvider<UserImportModel> {

    private static final Logger logger = LoggerFactory.getLogger(UserExportProvider.class);

    @Override
    public List<UserImportModel> queryExportData(Object params) {
        // 实际项目中应该从数据库查询
        // 示例：return userMapper.selectList(queryParams);
        
        logger.info("查询用户导出数据: params={}", params);
        
        // 模拟查询结果
        List<UserImportModel> users = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            UserImportModel user = new UserImportModel();
            user.setUsername("user" + i);
            user.setEmail("user" + i + "@example.com");
            user.setPhone("1380000000" + i);
            user.setAge(20 + i);
            user.setDepartment("技术部");
            users.add(user);
        }
        
        logger.info("用户导出数据查询完成: count={}", users.size());
        return users;
    }

    @Override
    public String getBusinessType() {
        return "USER_EXPORT";
    }

    @Override
    public Class<UserImportModel> getExcelModelClass() {
        return UserImportModel.class;
    }

    @Override
    public String getExportFileName() {
        return "user_export";
    }
}
