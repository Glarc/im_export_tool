# 架构流程图

## 1. 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         业务层 (Business Layer)                  │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐ │
│  │UserImportProcessor│  │UserExportProvider│  │ProductImport │ │
│  │                  │  │                  │  │   Processor  │ │
│  └────────┬─────────┘  └────────┬─────────┘  └──────┬───────┘ │
│           │实现                  │实现                │实现      │
└───────────┼──────────────────────┼────────────────────┼─────────┘
            │                      │                    │
┌───────────▼──────────────────────▼────────────────────▼─────────┐
│                       核心接口层 (Core Interfaces)                │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐ │
│  │ExcelRowProcessor │  │ExcelExportProvider│  │ExcelTemplate │ │
│  │                  │  │                  │  │   Provider   │ │
│  │- validateRow()   │  │- queryExport     │  │- getTemplate │ │
│  │- processValid    │  │  Data()          │  │  FileName()  │ │
│  │  Rows()          │  │- getExportFile   │  └──────────────┘ │
│  └──────────────────┘  │  Name()          │                    │
│                        └──────────────────┘                    │
└───────────────────────────────┬─────────────────────────────────┘
                                │使用
┌───────────────────────────────▼─────────────────────────────────┐
│                       服务层 (Service Layer)                      │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐ │
│  │ExcelImportService│  │ExcelExportService│  │ExcelTemplate │ │
│  │                  │  │                  │  │   Service    │ │
│  │- executeImport   │  │- executeExport   │  │- generate    │ │
│  │  Async()         │  │  Async()         │  │  Template()  │ │
│  │- doImport()      │  │- doExport()      │  └──────────────┘ │
│  └────────┬─────────┘  └────────┬─────────┘                    │
└───────────┼──────────────────────┼─────────────────────────────┘
            │                      │
            │ 依赖                 │ 依赖
            ▼                      ▼
┌──────────────────────┐  ┌──────────────────┐
│  EasyExcel (Alibaba) │  │FileStorageService│
│                      │  │                  │
│  - read()            │  │  - uploadFile()  │
│  - write()           │  │  - downloadFile()│
│                      │  │  - generateUrl() │
└──────────────────────┘  └────────┬─────────┘
                                   │
                                   ▼
                          ┌──────────────────┐
                          │       OSS        │
                          │ (DfsServiceStorage│
                          │     Client)      │
                          └──────────────────┘
```

## 2. 导入流程详细图

```
┌─────────┐
│  用户   │
└────┬────┘
     │ 1. 上传 Excel 文件
     ▼
┌─────────────────┐
│   OSS 存储      │
└────┬────────────┘
     │ fileUrl
     ▼
┌─────────────────────────────────────────┐
│ Controller/Service (业务调用层)          │
│                                         │
│ importService.executeImportAsync(       │
│     fileUrl,                            │
│     userImportProcessor,                │
│     "admin"                             │
│ )                                       │
└────┬────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────┐
│ ExcelImportService (核心服务层)          │
├─────────────────────────────────────────┤
│ ① 创建任务记录                           │
│    - taskStatus: PROCESSING             │
│    - businessType: USER_IMPORT          │
│                                         │
│ ② 下载文件                               │
│    fileStorage.downloadFile(fileUrl)    │
│                                         │
│ ③ 使用 EasyExcel 解析                    │
│    EasyExcel.read(inputStream, ...)     │
│                                         │
│ ④ 逐行校验并收集错误                      │
│    for each row:                        │
│      errorMsg = processor.validate      │
│                 Row(row, index)         │
│      if errorMsg != null:               │
│        errorRows.add(row)               │
│      else:                              │
│        validRows.add(row)               │
│                                         │
│ ⑤ 批量保存有效数据                        │
│    processor.processValidRows(validRows)│
│                                         │
│ ⑥ 生成错误 Excel（如有错误）              │
│    EasyExcel.write(errorRows)           │
│    fileStorage.uploadFile(...)          │
│                                         │
│ ⑦ 更新任务状态                           │
│    - taskStatus: SUCCESS                │
│    - totalRows: 1000                    │
│    - successRows: 980                   │
│    - errorRows: 20                      │
│    - errorFileUrl: oss://...            │
└────┬────────────────────────────────────┘
     │
     ▼
┌─────────────────┐
│ import_task_    │
│ history 表      │
└─────────────────┘
```

## 3. 导出流程详细图

```
┌─────────┐
│  用户   │
└────┬────┘
     │ 1. 请求导出
     ▼
┌─────────────────────────────────────────┐
│ Controller/Service (业务调用层)          │
│                                         │
│ exportService.executeExportAsync(       │
│     userExportProvider,                 │
│     queryParams,                        │
│     "admin"                             │
│ )                                       │
└────┬────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────┐
│ ExcelExportService (核心服务层)          │
├─────────────────────────────────────────┤
│ ① 创建任务记录                           │
│    - taskStatus: PROCESSING             │
│    - businessType: USER_EXPORT          │
│                                         │
│ ② 查询业务数据                           │
│    List<T> data = provider              │
│        .queryExportData(params)         │
│                                         │
│ ③ 使用 EasyExcel 生成 Excel              │
│    EasyExcel.write(outputStream, T)     │
│        .sheet("用户数据")                 │
│        .doWrite(data)                   │
│                                         │
│ ④ 上传到 OSS                             │
│    fileUrl = fileStorage                │
│        .uploadFile(excelStream)         │
│                                         │
│ ⑤ 更新任务状态                           │
│    - taskStatus: SUCCESS                │
│    - fileUrl: oss://...                 │
│    - totalRows: 5000                    │
└────┬────────────────────────────────────┘
     │
     ├──────────────┬──────────────┐
     ▼              ▼              ▼
┌─────────┐  ┌─────────┐  ┌────────────┐
│  OSS    │  │ export_ │  │   用户     │
│  存储   │  │ task_   │  │  下载文件  │
│         │  │ history │  │            │
└─────────┘  └─────────┘  └────────────┘
```

## 4. 核心接口关系图

```
┌─────────────────────────────────────────────────────────┐
│                  ExcelRowProcessor<T>                    │
├─────────────────────────────────────────────────────────┤
│ + validateRow(T data, int index): String               │
│ + processValidRows(List<T> validRows): void            │
│ + getBusinessType(): String                            │
│ + getExcelModelClass(): Class<T>                       │
└──────────────────┬──────────────────────────────────────┘
                   │
                   │ 实现
                   │
     ┌─────────────┼─────────────┬─────────────┐
     │             │             │             │
     ▼             ▼             ▼             ▼
┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐
│  User   │  │ Product │  │  Order  │  │  ...    │
│ Import  │  │ Import  │  │ Import  │  │         │
│Processor│  │Processor│  │Processor│  │         │
└─────────┘  └─────────┘  └─────────┘  └─────────┘

┌─────────────────────────────────────────────────────────┐
│                 ExcelExportProvider<T>                   │
├─────────────────────────────────────────────────────────┤
│ + queryExportData(Object params): List<T>              │
│ + getBusinessType(): String                            │
│ + getExcelModelClass(): Class<T>                       │
│ + getExportFileName(): String                          │
└──────────────────┬──────────────────────────────────────┘
                   │
                   │ 实现
                   │
     ┌─────────────┼─────────────┬─────────────┐
     │             │             │             │
     ▼             ▼             ▼             ▼
┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐
│  User   │  │ Product │  │  Order  │  │  ...    │
│ Export  │  │ Export  │  │ Export  │  │         │
│Provider │  │Provider │  │Provider │  │         │
└─────────┘  └─────────┘  └─────────┘  └─────────┘
```

## 5. 数据模型关系图

```
┌──────────────────────────────────────┐
│      ImportTaskHistory               │
├──────────────────────────────────────┤
│ + id: Long                           │
│ + businessType: String               │
│ + taskStatus: String                 │
│ + originalFileUrl: String            │
│ + totalRows: Integer                 │
│ + successRows: Integer               │
│ + errorRows: Integer                 │
│ + errorFileUrl: String               │
│ + errorMessage: String               │
│ + createdBy: String                  │
│ + createdTime: LocalDateTime         │
│ + updatedTime: LocalDateTime         │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│      ExportTaskHistory               │
├──────────────────────────────────────┤
│ + id: Long                           │
│ + businessType: String               │
│ + taskStatus: String                 │
│ + fileUrl: String                    │
│ + totalRows: Integer                 │
│ + queryParams: String                │
│ + errorMessage: String               │
│ + createdBy: String                  │
│ + createdTime: LocalDateTime         │
│ + updatedTime: LocalDateTime         │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│      ExcelRowError<T>                │
├──────────────────────────────────────┤
│ + rowIndex: int                      │
│ + rowData: T                         │
│ + errorMessage: String               │
└──────────────────────────────────────┘
```

## 6. 扩展新业务示意图

```
需要支持新业务（如：产品导入）

第1步：定义数据模型
┌──────────────────────────┐
│  ProductImportModel      │
├──────────────────────────┤
│ @ExcelProperty("产品名称")│
│ private String name;     │
│                          │
│ @ExcelProperty("价格")    │
│ private Double price;    │
└──────────────────────────┘

第2步：实现处理器接口
┌────────────────────────────────────┐
│  ProductImportProcessor            │
│  implements ExcelRowProcessor      │
├────────────────────────────────────┤
│ validateRow() {                    │
│   // 自定义校验逻辑                 │
│ }                                  │
│                                    │
│ processValidRows() {               │
│   // 批量保存到数据库               │
│ }                                  │
└────────────────────────────────────┘

第3步：直接使用
┌────────────────────────────────────┐
│  importService.executeImportAsync( │
│      fileUrl,                      │
│      productProcessor,             │
│      "admin"                       │
│  )                                 │
└────────────────────────────────────┘

✅ 无需修改核心代码！
✅ 所有核心功能自动继承！
```
