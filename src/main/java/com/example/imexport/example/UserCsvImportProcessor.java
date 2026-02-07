package com.example.imexport.example;

import com.example.imexport.core.CsvRowProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户 CSV 导入处理器示例
 * 演示如何实现 CSV 导入处理器
 */
@Component
public class UserCsvImportProcessor implements CsvRowProcessor<UserCsvModel> {

    private static final Logger logger = LoggerFactory.getLogger(UserCsvImportProcessor.class);

    @Override
    public String validateRow(UserCsvModel rowData, int rowIndex) {
        // 用户名校验
        if (rowData.getUsername() == null || rowData.getUsername().trim().isEmpty()) {
            return "用户名不能为空";
        }
        
        if (rowData.getUsername().length() > 50) {
            return "用户名长度不能超过50个字符";
        }

        // 邮箱校验
        if (rowData.getEmail() != null && !rowData.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "邮箱格式不正确";
        }

        // 手机号校验
        if (rowData.getPhone() != null && !rowData.getPhone().matches("^1[3-9]\\d{9}$")) {
            return "手机号格式不正确";
        }

        // 年龄校验
        if (rowData.getAge() != null && (rowData.getAge() < 0 || rowData.getAge() > 150)) {
            return "年龄必须在0-150之间";
        }

        return null; // 校验通过
    }

    @Override
    public void processValidRows(List<UserCsvModel> validRows) {
        // 实际项目中这里应该批量插入数据库
        // 示例代码仅打印日志
        logger.info("批量保存用户数据: count={}", validRows.size());
        
        // 模拟数据库操作
        // userMapper.batchInsert(validRows);
        
        for (UserCsvModel user : validRows) {
            logger.debug("保存用户: username={}, email={}, phone={}", 
                user.getUsername(), user.getEmail(), user.getPhone());
        }
    }

    @Override
    public String getBusinessType() {
        return "USER_CSV_IMPORT";
    }

    @Override
    public Class<UserCsvModel> getCsvModelClass() {
        return UserCsvModel.class;
    }

    @Override
    public String[] getCsvHeaders() {
        return new String[]{"用户名", "邮箱", "手机号", "年龄", "部门"};
    }
}
