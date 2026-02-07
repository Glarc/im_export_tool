package com.example.imexport.example;

import com.example.imexport.core.CsvExportProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户 CSV 导出提供者示例
 * 演示如何实现 CSV 导出数据提供者
 */
@Component
public class UserCsvExportProvider implements CsvExportProvider<UserCsvModel> {

    private static final Logger logger = LoggerFactory.getLogger(UserCsvExportProvider.class);

    @Override
    public List<UserCsvModel> queryExportData(Object params) {
        // 实际项目中这里应该从数据库查询数据
        // 示例代码返回模拟数据
        logger.info("查询用户导出数据: params={}", params);
        
        // 模拟数据库查询
        // return userMapper.selectList(queryWrapper);
        
        // 返回模拟数据
        List<UserCsvModel> mockData = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            UserCsvModel user = new UserCsvModel();
            user.setUsername("user" + i);
            user.setEmail("user" + i + "@example.com");
            user.setPhone("138000" + String.format("%05d", i));
            user.setAge(20 + i);
            user.setDepartment("技术部");
            mockData.add(user);
        }
        
        return mockData;
    }

    @Override
    public String getBusinessType() {
        return "USER_CSV_EXPORT";
    }

    @Override
    public Class<UserCsvModel> getCsvModelClass() {
        return UserCsvModel.class;
    }

    @Override
    public String getExportFileName() {
        return "user_csv_export";
    }

    @Override
    public String[] getCsvHeaders() {
        return new String[]{"用户名", "邮箱", "手机号", "年龄", "部门"};
    }
}
