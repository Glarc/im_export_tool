package com.example.imexport.example;

import com.example.imexport.service.ExcelImportService;
import com.example.imexport.service.ExcelExportService;
import com.example.imexport.service.ExcelTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 示例业务测试
 * 演示如何使用导入导出工具
 */
@SpringBootTest
public class UserImportExportTest {

    @Autowired(required = false)
    private ExcelImportService importService;

    @Autowired(required = false)
    private ExcelExportService exportService;

    @Autowired(required = false)
    private ExcelTemplateService templateService;

    @Autowired(required = false)
    private UserImportProcessor userImportProcessor;

    @Autowired(required = false)
    private UserExportProvider userExportProvider;

    @Autowired(required = false)
    private UserTemplateProvider userTemplateProvider;

    /**
     * 测试用户导入处理器的校验逻辑
     */
    @Test
    public void testUserValidation() {
        if (userImportProcessor == null) {
            System.out.println("跳过测试：需要数据库配置");
            return;
        }

        UserImportModel validUser = new UserImportModel();
        validUser.setUsername("testuser");
        validUser.setEmail("test@example.com");
        validUser.setPhone("13800138000");
        validUser.setAge(25);
        validUser.setDepartment("技术部");

        String validationResult = userImportProcessor.validateRow(validUser, 1);
        assert validationResult == null : "有效数据应该通过校验";
        System.out.println("✓ 有效用户数据校验通过");

        // 测试无效数据
        UserImportModel invalidUser = new UserImportModel();
        invalidUser.setUsername("");
        invalidUser.setEmail("invalid-email");
        
        String errorMsg = userImportProcessor.validateRow(invalidUser, 2);
        assert errorMsg != null : "无效数据应该返回错误信息";
        System.out.println("✓ 无效用户数据校验失败（预期）: " + errorMsg);
    }

    /**
     * 测试导出数据查询
     */
    @Test
    public void testExportDataQuery() {
        if (userExportProvider == null) {
            System.out.println("跳过测试：需要数据库配置");
            return;
        }

        java.util.List<UserImportModel> exportData = userExportProvider.queryExportData(null);
        assert exportData != null : "导出数据不应为null";
        assert !exportData.isEmpty() : "应该返回模拟数据";
        System.out.println("✓ 导出数据查询成功，数据行数: " + exportData.size());
    }

    /**
     * 演示完整的导入流程（不连接数据库）
     */
    @Test
    public void demonstrateImportFlow() {
        System.out.println("\n=== 导入流程演示 ===");
        System.out.println("1. 上传 Excel 文件到 OSS");
        System.out.println("   - 文件URL: oss://bucket/import/users.xlsx");
        
        System.out.println("\n2. 调用导入服务");
        System.out.println("   importService.executeImportAsync(fileUrl, userProcessor, \"admin\")");
        
        System.out.println("\n3. 系统自动执行:");
        System.out.println("   ✓ 创建导入任务记录（状态：PROCESSING）");
        System.out.println("   ✓ 从 OSS 下载文件");
        System.out.println("   ✓ 使用 EasyExcel 逐行解析");
        System.out.println("   ✓ 调用 validateRow() 校验每一行");
        System.out.println("   ✓ 收集有效行和错误行");
        System.out.println("   ✓ 批量保存有效数据（调用 processValidRows()）");
        System.out.println("   ✓ 生成错误 Excel 并上传 OSS");
        System.out.println("   ✓ 更新任务状态（SUCCESS）");
        
        System.out.println("\n4. 返回结果:");
        System.out.println("   - 任务ID: 12345");
        System.out.println("   - 总行数: 1000");
        System.out.println("   - 成功行数: 980");
        System.out.println("   - 错误行数: 20");
        System.out.println("   - 错误文件: oss://bucket/error/error_USER_IMPORT_20260207.xlsx");
    }

    /**
     * 演示完整的导出流程（不连接数据库）
     */
    @Test
    public void demonstrateExportFlow() {
        System.out.println("\n=== 导出流程演示 ===");
        System.out.println("1. 调用导出服务");
        System.out.println("   exportService.executeExportAsync(userProvider, params, \"admin\")");
        
        System.out.println("\n2. 系统自动执行:");
        System.out.println("   ✓ 创建导出任务记录（状态：PROCESSING）");
        System.out.println("   ✓ 调用 queryExportData() 查询业务数据");
        System.out.println("   ✓ 使用 EasyExcel 生成 Excel 文件");
        System.out.println("   ✓ 上传文件到 OSS");
        System.out.println("   ✓ 更新任务状态（SUCCESS）及文件地址");
        
        System.out.println("\n3. 返回结果:");
        System.out.println("   - 任务ID: 12346");
        System.out.println("   - 文件URL: oss://bucket/export/user_export_20260207.xlsx");
        System.out.println("   - 数据行数: 5000");
    }

    /**
     * 演示模板下载流程（不连接数据库）
     */
    @Test
    public void demonstrateTemplateFlow() {
        System.out.println("\n=== 模板下载流程演示 ===");
        System.out.println("1. 调用模板服务");
        System.out.println("   templateService.generateTemplateDownloadUrl(userTemplateProvider)");
        
        System.out.println("\n2. 系统自动执行:");
        System.out.println("   ✓ 根据数据模型生成空白 Excel（仅包含表头）");
        System.out.println("   ✓ 上传到 OSS");
        System.out.println("   ✓ 生成临时访问 URL（1小时有效）");
        
        System.out.println("\n3. 返回结果:");
        System.out.println("   - 模板URL: https://oss.example.com/template/user_import_template.xlsx?expires=3600");
        System.out.println("   - 用户下载模板填写数据后，上传进行导入");
    }

    /**
     * 演示如何扩展新业务
     */
    @Test
    public void demonstrateExtension() {
        System.out.println("\n=== 扩展新业务演示 ===");
        System.out.println("假设需要支持 \"产品导入\" 业务：");
        
        System.out.println("\n步骤1: 定义数据模型");
        System.out.println("```java");
        System.out.println("@Data");
        System.out.println("public class ProductImportModel {");
        System.out.println("    @ExcelProperty(value = \"产品名称\", index = 0)");
        System.out.println("    private String productName;");
        System.out.println("    ");
        System.out.println("    @ExcelProperty(value = \"价格\", index = 1)");
        System.out.println("    private Double price;");
        System.out.println("}");
        System.out.println("```");
        
        System.out.println("\n步骤2: 实现处理器");
        System.out.println("```java");
        System.out.println("@Component");
        System.out.println("public class ProductImportProcessor implements ExcelRowProcessor<ProductImportModel> {");
        System.out.println("    @Override");
        System.out.println("    public String validateRow(ProductImportModel rowData, int rowIndex) {");
        System.out.println("        if (rowData.getPrice() <= 0) return \"价格必须大于0\";");
        System.out.println("        return null;");
        System.out.println("    }");
        System.out.println("    ");
        System.out.println("    @Override");
        System.out.println("    public void processValidRows(List<ProductImportModel> validRows) {");
        System.out.println("        productMapper.batchInsert(validRows);");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("```");
        
        System.out.println("\n步骤3: 使用");
        System.out.println("```java");
        System.out.println("importService.executeImportAsync(fileUrl, productProcessor, \"admin\");");
        System.out.println("```");
        
        System.out.println("\n无需修改核心代码，即可支持新业务！");
    }
}
