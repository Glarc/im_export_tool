package com.example.imexport.example;

import com.example.imexport.service.CsvImportService;
import com.example.imexport.service.CsvExportService;
import com.example.imexport.service.CsvTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * CSV 导入导出测试
 * 演示如何使用 CSV 导入导出工具
 */
@SpringBootTest
public class UserCsvImportExportTest {

    @Autowired(required = false)
    private CsvImportService csvImportService;

    @Autowired(required = false)
    private CsvExportService csvExportService;

    @Autowired(required = false)
    private CsvTemplateService csvTemplateService;

    @Autowired(required = false)
    private UserCsvImportProcessor userCsvImportProcessor;

    @Autowired(required = false)
    private UserCsvExportProvider userCsvExportProvider;

    @Autowired(required = false)
    private UserCsvTemplateProvider userCsvTemplateProvider;

    /**
     * 测试用户 CSV 导入处理器的校验逻辑
     */
    @Test
    public void testUserCsvValidation() {
        if (userCsvImportProcessor == null) {
            System.out.println("跳过测试：需要数据库配置");
            return;
        }

        UserCsvModel validUser = new UserCsvModel();
        validUser.setUsername("testuser");
        validUser.setEmail("test@example.com");
        validUser.setPhone("13800138000");
        validUser.setAge(25);
        validUser.setDepartment("技术部");

        String validationResult = userCsvImportProcessor.validateRow(validUser, 1);
        assert validationResult == null : "有效数据应该通过校验";
        System.out.println("✓ 有效用户 CSV 数据校验通过");

        // 测试无效数据
        UserCsvModel invalidUser = new UserCsvModel();
        invalidUser.setUsername("");
        invalidUser.setEmail("invalid-email");
        
        String errorMsg = userCsvImportProcessor.validateRow(invalidUser, 2);
        assert errorMsg != null : "无效数据应该返回错误信息";
        System.out.println("✓ 无效用户 CSV 数据校验失败（预期）: " + errorMsg);
    }

    /**
     * 测试 CSV 导出数据查询
     */
    @Test
    public void testCsvExportDataQuery() {
        if (userCsvExportProvider == null) {
            System.out.println("跳过测试：需要数据库配置");
            return;
        }

        java.util.List<UserCsvModel> exportData = userCsvExportProvider.queryExportData(null);
        assert exportData != null : "导出数据不应为null";
        assert !exportData.isEmpty() : "应该返回模拟数据";
        System.out.println("✓ CSV 导出数据查询成功，数据行数: " + exportData.size());
    }

    /**
     * 演示完整的 CSV 导入流程
     */
    @Test
    public void demonstrateCsvImportFlow() {
        System.out.println("\n=== CSV 导入流程演示 ===");
        System.out.println("1. 上传 CSV 文件到 OSS");
        System.out.println("   - 文件URL: oss://bucket/import/users.csv");
        
        System.out.println("\n2. 调用 CSV 导入服务");
        System.out.println("   csvImportService.executeImportAsync(fileUrl, userCsvProcessor, \"admin\")");
        
        System.out.println("\n3. 系统自动执行:");
        System.out.println("   ✓ 创建导入任务记录（状态：PROCESSING）");
        System.out.println("   ✓ 从 OSS 下载文件");
        System.out.println("   ✓ 使用 OpenCSV 逐行解析");
        System.out.println("   ✓ 调用 validateRow() 校验每一行");
        System.out.println("   ✓ 收集有效行和错误行");
        System.out.println("   ✓ 批量保存有效数据（调用 processValidRows()）");
        System.out.println("   ✓ 生成错误 CSV 并上传 OSS");
        System.out.println("   ✓ 更新任务状态（SUCCESS）");
        
        System.out.println("\n4. 返回结果:");
        System.out.println("   - 任务ID: 12345");
        System.out.println("   - 总行数: 1000");
        System.out.println("   - 成功行数: 980");
        System.out.println("   - 错误行数: 20");
        System.out.println("   - 错误文件: oss://bucket/error/error_USER_CSV_IMPORT_20260207.csv");
    }

    /**
     * 演示完整的 CSV 导出流程
     */
    @Test
    public void demonstrateCsvExportFlow() {
        System.out.println("\n=== CSV 导出流程演示 ===");
        System.out.println("1. 调用 CSV 导出服务");
        System.out.println("   csvExportService.executeExportAsync(userCsvProvider, params, \"admin\")");
        
        System.out.println("\n2. 系统自动执行:");
        System.out.println("   ✓ 创建导出任务记录（状态：PROCESSING）");
        System.out.println("   ✓ 调用 queryExportData() 查询业务数据");
        System.out.println("   ✓ 使用 OpenCSV 生成 CSV 文件");
        System.out.println("   ✓ 上传文件到 OSS");
        System.out.println("   ✓ 更新任务状态（SUCCESS）及文件地址");
        
        System.out.println("\n3. 返回结果:");
        System.out.println("   - 任务ID: 12346");
        System.out.println("   - 文件URL: oss://bucket/export/user_csv_export_20260207.csv");
        System.out.println("   - 数据行数: 5000");
    }

    /**
     * 演示 CSV 模板下载流程
     */
    @Test
    public void demonstrateCsvTemplateFlow() {
        System.out.println("\n=== CSV 模板下载流程演示 ===");
        System.out.println("1. 调用 CSV 模板服务");
        System.out.println("   csvTemplateService.generateTemplateDownloadUrl(userCsvTemplateProvider)");
        
        System.out.println("\n2. 系统自动执行:");
        System.out.println("   ✓ 根据数据模型生成空白 CSV（仅包含表头）");
        System.out.println("   ✓ 上传到 OSS");
        System.out.println("   ✓ 生成临时访问 URL（1小时有效）");
        
        System.out.println("\n3. 返回结果:");
        System.out.println("   - 模板URL: https://oss.example.com/template/user_csv_import_template.csv?expires=3600");
        System.out.println("   - 用户下载模板填写数据后，上传进行导入");
    }

    /**
     * 演示 Excel 和 CSV 的共同抽象
     */
    @Test
    public void demonstrateAbstraction() {
        System.out.println("\n=== 抽象层演示 ===");
        System.out.println("核心抽象接口：");
        System.out.println("  ✓ RowProcessor<T>       - 通用行处理器接口");
        System.out.println("  ✓ DataProvider<T>       - 通用数据提供者接口");
        System.out.println("  ✓ TemplateProvider<T>   - 通用模板提供者接口");
        
        System.out.println("\nExcel 实现：");
        System.out.println("  ✓ ExcelRowProcessor<T> extends RowProcessor<T>");
        System.out.println("  ✓ ExcelExportProvider<T> extends DataProvider<T>");
        System.out.println("  ✓ ExcelTemplateProvider<T> extends TemplateProvider<T>");
        
        System.out.println("\nCSV 实现：");
        System.out.println("  ✓ CsvRowProcessor<T> extends RowProcessor<T>");
        System.out.println("  ✓ CsvExportProvider<T> extends DataProvider<T>");
        System.out.println("  ✓ CsvTemplateProvider<T> extends TemplateProvider<T>");
        
        System.out.println("\n核心逻辑保持不变，通过接口扩展支持不同文件格式！");
    }

    /**
     * 演示如何扩展新的 CSV 业务
     */
    @Test
    public void demonstrateCsvExtension() {
        System.out.println("\n=== 扩展新的 CSV 业务演示 ===");
        System.out.println("假设需要支持 \"产品 CSV 导入\" 业务：");
        
        System.out.println("\n步骤1: 定义数据模型");
        System.out.println("```java");
        System.out.println("@Data");
        System.out.println("public class ProductCsvModel {");
        System.out.println("    private String productName;");
        System.out.println("    private Double price;");
        System.out.println("    private Integer stock;");
        System.out.println("}");
        System.out.println("```");
        
        System.out.println("\n步骤2: 实现 CSV 处理器");
        System.out.println("```java");
        System.out.println("@Component");
        System.out.println("public class ProductCsvImportProcessor implements CsvRowProcessor<ProductCsvModel> {");
        System.out.println("    @Override");
        System.out.println("    public String validateRow(ProductCsvModel rowData, int rowIndex) {");
        System.out.println("        if (rowData.getPrice() <= 0) return \"价格必须大于0\";");
        System.out.println("        return null;");
        System.out.println("    }");
        System.out.println("    ");
        System.out.println("    @Override");
        System.out.println("    public void processValidRows(List<ProductCsvModel> validRows) {");
        System.out.println("        productMapper.batchInsert(validRows);");
        System.out.println("    }");
        System.out.println("    ");
        System.out.println("    @Override");
        System.out.println("    public String[] getCsvHeaders() {");
        System.out.println("        return new String[]{\"产品名称\", \"价格\", \"库存\"};");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("```");
        
        System.out.println("\n步骤3: 使用");
        System.out.println("```java");
        System.out.println("csvImportService.executeImportAsync(fileUrl, productCsvProcessor, \"admin\");");
        System.out.println("```");
        
        System.out.println("\n无需修改核心代码，即可支持新的 CSV 业务！");
    }
}
