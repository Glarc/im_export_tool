# Import/Export Tool - Excel 导入导出工具

[![Java](https://img.shields.io/badge/Java-1.8-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.14-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![EasyExcel](https://img.shields.io/badge/EasyExcel-3.3.2-orange.svg)](https://github.com/alibaba/easyexcel)

基于 **Alibaba EasyExcel** 和 **OSS** 的通用导入导出工具，支持多业务场景、异步任务、错误收集等特性。

## ✨ 核心特性

- 🚀 **高度抽象**：核心流程与业务解耦，业务通过接口扩展
- 📊 **EasyExcel 集成**：高性能 Excel 处理，支持大文件
- ☁️ **OSS 存储**：文件统一存储在 OSS，支持 DfsServiceStorageClient
- ⚡ **异步任务**：支持异步导入导出，不阻塞主线程
- ❌ **错误收集**：自动生成错误 Excel，包含错误行和错误信息
- 📝 **模板下载**：自动生成导入模板
- 📈 **任务追踪**：完整的任务历史记录，支持状态查询
- 🔧 **灵活扩展**：预留 CSV 等格式扩展点

## 📦 技术栈

- **Spring Boot 2.7.14** - 应用框架
- **Alibaba EasyExcel 3.3.2** - Excel 处理
- **MyBatis Plus 3.5.3** - 数据访问
- **MySQL** - 数据存储
- **OSS** - 文件存储（通过 DfsServiceStorageClient）

## 🏗️ 架构设计

详细架构文档请参考：[ARCHITECTURE.md](./ARCHITECTURE.md)

### 模块划分

```
im-export-tool
├── core/              # 核心接口定义
│   ├── ExcelRowProcessor       # 导入行处理器
│   ├── ExcelExportProvider     # 导出数据提供者
│   └── ExcelTemplateProvider   # 模板提供者
├── service/           # 服务实现
│   ├── ExcelImportService      # 导入服务
│   ├── ExcelExportService      # 导出服务
│   └── ExcelTemplateService    # 模板服务
├── storage/           # 存储层
│   ├── FileStorageService      # 存储接口
│   └── FileStorageServiceImpl  # 存储实现（OSS）
├── model/             # 数据模型
│   ├── ImportTaskHistory       # 导入任务历史
│   └── ExportTaskHistory       # 导出任务历史
└── example/           # 示例业务
    ├── UserImportModel         # 用户数据模型
    ├── UserImportProcessor     # 用户导入处理器
    ├── UserExportProvider      # 用户导出提供者
    └── UserTemplateProvider    # 用户模板提供者
```

## 🚀 快速开始

### 1. 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+

### 2. 数据库初始化

执行 SQL 脚本创建表：

```bash
mysql -u root -p < src/main/resources/schema.sql
```

### 3. 配置文件

修改 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/im_export
    username: your_username
    password: your_password
```

### 4. 构建运行

```bash
# 构建项目
mvn clean package

# 运行应用
mvn spring-boot:run
```

## 📖 使用指南

### 导入功能

#### 1. 定义数据模型

```java
@Data
public class UserImportModel {
    @ExcelProperty(value = "用户名", index = 0)
    private String username;
    
    @ExcelProperty(value = "邮箱", index = 1)
    private String email;
    
    @ExcelProperty(value = "手机号", index = 2)
    private String phone;
}
```

#### 2. 实现导入处理器

```java
@Component
public class UserImportProcessor implements ExcelRowProcessor<UserImportModel> {
    
    @Override
    public String validateRow(UserImportModel rowData, int rowIndex) {
        // 校验逻辑
        if (rowData.getUsername() == null) {
            return "用户名不能为空";
        }
        return null; // 校验通过
    }
    
    @Override
    public void processValidRows(List<UserImportModel> validRows) {
        // 批量保存到数据库
        userMapper.batchInsert(validRows);
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
```

#### 3. 调用导入服务

```java
@Autowired
private ExcelImportService importService;

@Autowired
private UserImportProcessor userProcessor;

public Long importUsers(String fileUrl) {
    // 异步导入
    return importService.executeImportAsync(fileUrl, userProcessor, "admin");
}
```

### 导出功能

#### 1. 实现导出提供者

```java
@Component
public class UserExportProvider implements ExcelExportProvider<UserImportModel> {
    
    @Override
    public List<UserImportModel> queryExportData(Object params) {
        // 从数据库查询数据
        return userMapper.selectList(queryParams);
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
```

#### 2. 调用导出服务

```java
@Autowired
private ExcelExportService exportService;

@Autowired
private UserExportProvider userProvider;

public Long exportUsers(Object params) {
    // 异步导出
    return exportService.executeExportAsync(userProvider, params, "admin");
}
```

### 模板下载

#### 1. 实现模板提供者

```java
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
```

#### 2. 生成模板下载链接

```java
@Autowired
private ExcelTemplateService templateService;

@Autowired
private UserTemplateProvider userTemplateProvider;

public String getTemplateUrl() {
    // 生成临时下载链接（1小时有效）
    return templateService.generateTemplateDownloadUrl(userTemplateProvider);
}
```

## 🔄 导入流程

```
上传文件 → 创建任务 → 解析Excel → 校验数据 → 收集错误 
  → 保存有效数据 → 生成错误文件 → 更新任务状态
```

- ✅ **成功行**：批量保存到数据库
- ❌ **错误行**：生成包含错误信息的 Excel 文件上传到 OSS

## 📊 导出流程

```
创建任务 → 查询数据 → 生成Excel → 上传OSS → 更新任务状态
```

## 🗄️ 数据库设计

### 导入任务历史表

```sql
CREATE TABLE import_task_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  business_type VARCHAR(50) NOT NULL,
  task_status VARCHAR(20) NOT NULL,
  original_file_url VARCHAR(500),
  total_rows INT,
  success_rows INT,
  error_rows INT,
  error_file_url VARCHAR(500),
  error_message TEXT,
  created_by VARCHAR(50),
  created_time DATETIME,
  updated_time DATETIME
);
```

### 导出任务历史表

```sql
CREATE TABLE export_task_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  business_type VARCHAR(50) NOT NULL,
  task_status VARCHAR(20) NOT NULL,
  file_url VARCHAR(500),
  total_rows INT,
  query_params TEXT,
  error_message TEXT,
  created_by VARCHAR(50),
  created_time DATETIME,
  updated_time DATETIME
);
```

## 🔌 OSS 集成

默认实现为模拟 OSS，实际项目中需要对接真实的 DfsServiceStorageClient：

```java
@Service
public class FileStorageServiceImpl implements FileStorageService {
    
    @Autowired
    private DfsServiceStorageClient dfsClient; // 注入真实客户端
    
    @Override
    public String uploadFile(InputStream inputStream, String fileName, String contentType) {
        // 使用 DfsServiceStorageClient 上传
        return dfsClient.upload(inputStream, fileName, contentType);
    }
    
    @Override
    public InputStream downloadFile(String fileUrl) {
        // 使用 DfsServiceStorageClient 下载
        return dfsClient.download(fileUrl);
    }
}
```

## 🎯 扩展新业务

只需三步即可支持新的导入导出业务：

1. **定义数据模型**（使用 `@ExcelProperty` 注解）
2. **实现处理器/提供者接口**
3. **注入并调用服务**

无需修改核心代码！

## 🔮 未来扩展

### 支持 CSV 格式

当前架构已预留扩展点，可轻松支持 CSV：

```java
public interface FileImportService {
    <T> ImportResult importFile(String fileUrl, FileProcessor<T> processor);
}

@Service
public class CsvImportServiceImpl implements FileImportService {
    // CSV 实现
}
```

## 📄 License

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📧 联系方式

如有问题，请提交 Issue 或联系项目维护者。