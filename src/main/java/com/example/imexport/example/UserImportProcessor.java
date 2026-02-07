package com.example.imexport.example;

import com.example.imexport.core.ExcelRowProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 示例业务 - 用户导入处理器
 * 展示如何实现 ExcelRowProcessor 接口
 */
@Component
public class UserImportProcessor implements ExcelRowProcessor<UserImportModel> {

    private static final Logger logger = LoggerFactory.getLogger(UserImportProcessor.class);
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public String validateRow(UserImportModel rowData, int rowIndex) {
        // 校验用户名
        if (rowData.getUsername() == null || rowData.getUsername().trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (rowData.getUsername().length() > 50) {
            return "用户名长度不能超过50个字符";
        }

        // 校验邮箱
        if (rowData.getEmail() == null || rowData.getEmail().trim().isEmpty()) {
            return "邮箱不能为空";
        }
        if (!EMAIL_PATTERN.matcher(rowData.getEmail()).matches()) {
            return "邮箱格式不正确";
        }

        // 校验手机号（可选）
        if (rowData.getPhone() != null && !rowData.getPhone().trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(rowData.getPhone()).matches()) {
                return "手机号格式不正确";
            }
        }

        // 校验年龄（可选）
        if (rowData.getAge() != null) {
            if (rowData.getAge() < 0 || rowData.getAge() > 150) {
                return "年龄必须在0-150之间";
            }
        }

        return null; // 校验通过
    }

    @Override
    public void processValidRows(List<UserImportModel> validRows) {
        // 实际项目中应该批量插入数据库
        // 示例：userMapper.batchInsert(validRows);
        
        logger.info("开始保存用户数据: count={}", validRows.size());
        
        for (UserImportModel user : validRows) {
            // 模拟保存逻辑
            logger.debug("保存用户: username={}, email={}", user.getUsername(), user.getEmail());
        }
        
        logger.info("用户数据保存完成: count={}", validRows.size());
    }

    @Override
    public String getBusinessType() {
        return "USER_IMPORT";
    }

    @Override
    public Class<UserImportModel> getExcelModelClass() {
        return UserImportModel.class;
    }
}
