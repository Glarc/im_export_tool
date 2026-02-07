# Import/Export Tool - 架构设计文档

## 一、概述

本项目实现了一个基于 **Alibaba EasyExcel** 和 **OSS** 的通用导入导出工具，支持多业务场景的 Excel 数据处理，具有高度抽象和灵活扩展的特点。

### 技术栈

- **Excel 处理**: Alibaba EasyExcel 3.3.2
- **文件存储**: OSS (通过 DfsServiceStorageClient)
- **任务执行**: Spring @Async 异步任务
- **数据库**: MyBatis Plus
- **框架**: Spring Boot 2.7.14

## 二、模块划分

### 2.1 核心模块 (core)

定义了导入导出的核心接口，业务通过实现这些接口来扩展功能：

```
com.example.imexport.core
├── ExcelRowProcessor        # 导入行处理器接口
├── ExcelExportProvider      # 导出数据提供者接口
└── ExcelTemplateProvider    # 模板提供者接口
```

### 2.2 服务层 (service)

提供核心导入导出功能的实现：

```
com.example.imexport.service
├── ExcelImportService       # 导入服务（解析、校验、错误收集）
├── ExcelExportService       # 导出服务（查询、生成、上传）
└── ExcelTemplateService     # 模板服务（生成、下载）
```

### 2.3 存储层 (storage)

封装 OSS 文件存储操作：

```
com.example.imexport.storage
├── FileStorageService       # 存储服务接口
└── FileStorageServiceImpl   # 存储服务实现（对接 DfsServiceStorageClient）
```

### 2.4 数据层 (model & mapper)

定义任务历史实体和数据访问：

```
com.example.imexport.model
├── ImportTaskHistory        # 导入任务历史
├── ExportTaskHistory        # 导出任务历史
├── ExcelRowError           # 错误行包装类
└── ImportResult            # 导入结果

com.example.imexport.mapper
├── ImportTaskHistoryMapper  # 导入任务 Mapper
└── ExportTaskHistoryMapper  # 导出任务 Mapper
```

### 2.5 示例业务 (example)

展示如何实现具体业务：

```
com.example.imexport.example
├── UserImportModel          # 用户导入数据模型
├── UserImportProcessor      # 用户导入处理器
├── UserExportProvider       # 用户导出提供者
└── UserTemplateProvider     # 用户模板提供者
```

## 三、核心接口设计

### 3.1 ExcelRowProcessor - 导入处理器

```java
public interface ExcelRowProcessor<T> {
    String validateRow(T rowData, int rowIndex);        // 行级校验
    void processValidRows(List<T> validRows);           // 处理有效数据
    String getBusinessType();                            // 业务标识
    Class<T> getExcelModelClass();                       // 数据模型类
}
```

**设计思想**：
- 业务只需关注 **校验规则** 和 **数据保存**，不关心 Excel 解析细节
- 支持自定义校验逻辑，返回错误信息
- 批量处理有效数据，提升性能

### 3.2 ExcelExportProvider - 导出提供者

```java
public interface ExcelExportProvider<T> {
    List<T> queryExportData(Object params);             // 查询导出数据
    String getBusinessType();                            // 业务标识
    Class<T> getExcelModelClass();                       // 数据模型类
    String getExportFileName();                          // 导出文件名
}
```

**设计思想**：
- 业务只需提供数据源，不关心 Excel 生成和上传
- 参数灵活，支持任意查询条件
- 统一文件命名规范

### 3.3 ExcelTemplateProvider - 模板提供者

```java
public interface ExcelTemplateProvider<T> {
    String getBusinessType();                            // 业务标识
    Class<T> getExcelModelClass();                       // 数据模型类
    String getTemplateFileName();                        // 模板文件名
}
```

**设计思想**：
- 根据数据模型自动生成模板（表头）
- 支持模板下载，方便用户填写

## 四、导入流程

```
1. 上传 Excel 文件到 OSS
    ↓
2. 创建导入任务记录（状态：PROCESSING）
    ↓
3. 从 OSS 下载文件
    ↓
4. 使用 EasyExcel 逐行解析
    ↓
5. 对每行数据调用 validateRow() 进行校验
    ↓
6. 收集有效行和错误行
    ↓
7. 批量保存有效数据（调用 processValidRows()）
    ↓
8. 如有错误行，生成错误 Excel 并上传 OSS
    ↓
9. 更新任务状态（SUCCESS/FAILED）及统计信息
```

### 导入核心代码

```java
@Service
public class ExcelImportService {
    
    @Async
    public <T> Long executeImportAsync(String fileUrl, 
                                        ExcelRowProcessor<T> processor, 
                                        String createdBy) {
        // 1. 创建任务
        ImportTaskHistory task = createTask(processor.getBusinessType(), fileUrl, createdBy);
        
        // 2. 执行导入
        ImportResult result = doImport(fileUrl, processor, task.getId());
        
        // 3. 更新任务
        updateTaskSuccess(task.getId(), result);
        
        return task.getId();
    }
    
    private <T> ImportResult doImport(String fileUrl, 
                                      ExcelRowProcessor<T> processor, 
                                      Long taskId) {
        List<T> validRows = new ArrayList<>();
        List<ExcelRowError<T>> errorRows = new ArrayList<>();
        
        // 使用 EasyExcel 解析
        EasyExcel.read(inputStream, processor.getExcelModelClass(), 
            new AnalysisEventListener<T>() {
                @Override
                public void invoke(T data, AnalysisContext context) {
                    String errorMsg = processor.validateRow(data, rowIndex);
                    if (errorMsg == null) {
                        validRows.add(data);
                    } else {
                        errorRows.add(new ExcelRowError<>(rowIndex, data, errorMsg));
                    }
                }
            }).sheet().doRead();
        
        // 保存有效数据
        processor.processValidRows(validRows);
        
        // 生成错误文件
        if (!errorRows.isEmpty()) {
            errorFileUrl = generateErrorFile(errorRows, processor);
        }
        
        return buildResult(validRows.size(), errorRows.size(), errorFileUrl);
    }
}
```

## 五、导出流程

```
1. 创建导出任务记录（状态：PROCESSING）
    ↓
2. 调用 queryExportData() 查询业务数据
    ↓
3. 使用 EasyExcel 生成 Excel 文件
    ↓
4. 上传文件到 OSS
    ↓
5. 更新任务状态及文件地址
```

### 导出核心代码

```java
@Service
public class ExcelExportService {
    
    @Async
    public <T> Long executeExportAsync(ExcelExportProvider<T> provider, 
                                        Object params, 
                                        String createdBy) {
        // 1. 创建任务
        ExportTaskHistory task = createTask(provider.getBusinessType(), params, createdBy);
        
        // 2. 执行导出
        String fileUrl = doExport(provider, params);
        
        // 3. 更新任务
        updateTaskSuccess(task.getId(), fileUrl, totalRows);
        
        return task.getId();
    }
    
    private <T> String doExport(ExcelExportProvider<T> provider, Object params) {
        // 查询数据
        List<T> data = provider.queryExportData(params);
        
        // 生成 Excel
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        EasyExcel.write(outputStream, provider.getExcelModelClass())
                 .sheet(provider.getBusinessType())
                 .doWrite(data);
        
        // 上传到 OSS
        String fileUrl = fileStorageService.uploadFile(inputStream, fileName, contentType);
        
        return fileUrl;
    }
}
```

## 六、扩展点设计

### 6.1 支持新业务场景

只需实现对应接口即可：

**导入新业务**：
```java
@Component
public class ProductImportProcessor implements ExcelRowProcessor<ProductModel> {
    @Override
    public String validateRow(ProductModel rowData, int rowIndex) {
        // 自定义校验逻辑
    }
    
    @Override
    public void processValidRows(List<ProductModel> validRows) {
        // 自定义保存逻辑
    }
}
```

**导出新业务**：
```java
@Component
public class ProductExportProvider implements ExcelExportProvider<ProductModel> {
    @Override
    public List<ProductModel> queryExportData(Object params) {
        // 自定义查询逻辑
    }
}
```

### 6.2 预留 CSV 扩展点

当前架构已预留扩展点：

1. **接口层**：核心接口与文件格式无关
2. **服务层**：可抽象出 `FileImportService` 和 `FileExportService`
3. **实现层**：添加 `CsvImportService` 和 `CsvExportService`

扩展方式：
```java
// 抽象接口
public interface FileImportService {
    <T> ImportResult importFile(String fileUrl, FileProcessor<T> processor);
}

// Excel 实现
@Service
public class ExcelImportServiceImpl implements FileImportService {
    // 使用 EasyExcel
}

// CSV 实现（未来）
@Service
public class CsvImportServiceImpl implements FileImportService {
    // 使用 OpenCSV 或其他库
}
```

## 七、架构优势

1. **高度抽象**：核心流程与业务解耦，业务通过接口扩展
2. **灵活扩展**：新增业务无需修改核心代码
3. **统一管理**：所有任务状态统一记录，便于追溯
4. **错误友好**：自动生成错误 Excel，用户可快速定位问题
5. **异步处理**：大文件导入导出不阻塞主线程
6. **OSS 集成**：文件统一存储在 OSS，节省服务器空间
7. **预留扩展**：支持未来 CSV、PDF 等格式

## 八、使用示例

### 8.1 导入使用

```java
@Autowired
private ExcelImportService importService;

@Autowired
private UserImportProcessor userProcessor;

public void importUsers(String fileUrl) {
    Long taskId = importService.executeImportAsync(fileUrl, userProcessor, "admin");
    // 返回任务ID，前端可轮询任务状态
}
```

### 8.2 导出使用

```java
@Autowired
private ExcelExportService exportService;

@Autowired
private UserExportProvider userProvider;

public void exportUsers(Object params) {
    Long taskId = exportService.executeExportAsync(userProvider, params, "admin");
    // 返回任务ID，前端可轮询任务状态
}
```

### 8.3 模板下载

```java
@Autowired
private ExcelTemplateService templateService;

@Autowired
private UserTemplateProvider userTemplateProvider;

public String downloadTemplate() {
    return templateService.generateTemplateDownloadUrl(userTemplateProvider);
    // 返回临时下载链接
}
```

## 九、数据库设计

### 9.1 导入任务历史表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| business_type | VARCHAR(50) | 业务类型（如 USER_IMPORT） |
| task_status | VARCHAR(20) | 任务状态（PROCESSING/SUCCESS/FAILED） |
| original_file_url | VARCHAR(500) | 原始文件 OSS 地址 |
| total_rows | INT | 总行数 |
| success_rows | INT | 成功行数 |
| error_rows | INT | 错误行数 |
| error_file_url | VARCHAR(500) | 错误文件 OSS 地址 |
| error_message | TEXT | 错误信息 |
| created_by | VARCHAR(50) | 创建人 |
| created_time | DATETIME | 创建时间 |
| updated_time | DATETIME | 更新时间 |

### 9.2 导出任务历史表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| business_type | VARCHAR(50) | 业务类型（如 USER_EXPORT） |
| task_status | VARCHAR(20) | 任务状态（PROCESSING/SUCCESS/FAILED） |
| file_url | VARCHAR(500) | 导出文件 OSS 地址 |
| total_rows | INT | 导出行数 |
| query_params | TEXT | 查询参数（JSON） |
| error_message | TEXT | 错误信息 |
| created_by | VARCHAR(50) | 创建人 |
| created_time | DATETIME | 创建时间 |
| updated_time | DATETIME | 更新时间 |

## 十、总结

本项目通过清晰的模块划分、灵活的接口设计和完善的流程控制，实现了一个可复用、易扩展的导入导出工具。业务开发者只需关注自己的校验和数据处理逻辑，无需关心 Excel 解析、OSS 上传、任务管理等通用功能，大大提升了开发效率。
