package com.example.imexport.service;

import com.example.imexport.core.CsvRowProcessor;
import com.example.imexport.mapper.ImportTaskHistoryMapper;
import com.example.imexport.model.ExcelRowError;
import com.example.imexport.model.ImportResult;
import com.example.imexport.model.ImportTaskHistory;
import com.example.imexport.storage.FileStorageService;
import com.example.imexport.util.CsvUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV 导入服务核心类
 * 提供通用的 CSV 导入流程：解析 -> 校验 -> 收集错误 -> 保存有效数据 -> 生成错误文件
 */
@Service
public class CsvImportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvImportService.class);

    @Autowired
    private ImportTaskHistoryMapper importTaskHistoryMapper;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 异步执行导入任务
     *
     * @param fileUrl 原始文件 OSS 地址
     * @param processor 业务处理器
     * @param createdBy 创建人
     * @param <T> CSV 数据模型类型
     * @return 任务ID
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public <T> Long executeImportAsync(String fileUrl, CsvRowProcessor<T> processor, String createdBy) {
        // 创建任务记录
        ImportTaskHistory task = createTask(processor.getBusinessType(), fileUrl, createdBy);
        
        try {
            // 执行导入
            ImportResult result = doImport(fileUrl, processor, task.getId());
            
            // 更新任务状态
            updateTaskSuccess(task.getId(), result);
            
            logger.info("CSV导入任务完成: taskId={}, result={}", task.getId(), result);
            return task.getId();
            
        } catch (Exception e) {
            logger.error("CSV导入任务失败: taskId={}", task.getId(), e);
            updateTaskFailed(task.getId(), e.getMessage());
            throw new RuntimeException("CSV导入任务失败", e);
        }
    }

    /**
     * 同步执行导入（用于测试或小数据量）
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> ImportResult executeImportSync(String fileUrl, CsvRowProcessor<T> processor, String createdBy) {
        ImportTaskHistory task = createTask(processor.getBusinessType(), fileUrl, createdBy);
        
        try {
            ImportResult result = doImport(fileUrl, processor, task.getId());
            updateTaskSuccess(task.getId(), result);
            return result;
        } catch (Exception e) {
            logger.error("CSV导入任务失败: taskId={}", task.getId(), e);
            updateTaskFailed(task.getId(), e.getMessage());
            throw new RuntimeException("CSV导入任务失败", e);
        }
    }

    /**
     * 核心导入逻辑
     */
    private <T> ImportResult doImport(String fileUrl, CsvRowProcessor<T> processor, Long taskId) {
        ImportResult result = new ImportResult();
        result.setTaskId(taskId);

        List<T> validRows = new ArrayList<>();
        List<ExcelRowError<T>> errorRows = new ArrayList<>();

        try {
            // 下载文件
            InputStream inputStream = fileStorageService.downloadFile(fileUrl);

            // 使用 CsvUtils 解析
            List<T> allRows = CsvUtils.readCsv(inputStream, processor.getModelClass(), processor.getCsvHeaders());

            // 校验每一行
            int rowIndex = 0;
            for (T data : allRows) {
                rowIndex++;
                
                // 校验数据
                String errorMsg = processor.validateRow(data, rowIndex);
                
                if (errorMsg == null || errorMsg.isEmpty()) {
                    validRows.add(data);
                } else {
                    errorRows.add(new ExcelRowError<>(rowIndex, data, errorMsg));
                }
            }

            logger.info("CSV 解析完成: totalRows={}, validRows={}, errorRows={}", 
                rowIndex, validRows.size(), errorRows.size());

        } catch (Exception e) {
            logger.error("CSV 解析失败", e);
            throw new RuntimeException("CSV 解析失败", e);
        }

        // 统计结果
        int totalRows = validRows.size() + errorRows.size();
        result.setTotalRows(totalRows);
        result.setSuccessRows(validRows.size());
        result.setErrorRows(errorRows.size());

        // 保存有效数据
        if (!validRows.isEmpty()) {
            processor.processValidRows(validRows);
        }

        // 生成错误文件
        if (!errorRows.isEmpty()) {
            String errorFileUrl = generateErrorFile(errorRows, processor);
            result.setErrorFileUrl(errorFileUrl);
        }

        result.setSuccess(errorRows.isEmpty());
        result.setMessage(errorRows.isEmpty() ? "导入成功" : "导入完成，存在错误行");

        return result;
    }

    /**
     * 生成错误 CSV 文件
     */
    private <T> String generateErrorFile(List<ExcelRowError<T>> errorRows, CsvRowProcessor<T> processor) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // 创建包含错误信息的数据
            List<String[]> errorData = new ArrayList<>();
            for (ExcelRowError<T> error : errorRows) {
                // 构建错误行: 行号 + 错误信息 + 原始数据
                String[] errorRow = buildErrorRow(error, processor.getCsvHeaders().length);
                errorData.add(errorRow);
            }

            // 构建表头：行号 + 错误信息 + 原始列名
            String[] errorHeaders = buildErrorHeaders(processor.getCsvHeaders());

            // 写入 CSV
            CsvUtils.writeCsv(outputStream, convertToObjectList(errorData), errorHeaders);

            // 上传到 OSS
            String fileName = "error_" + processor.getBusinessType() + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
            
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            String errorFileUrl = fileStorageService.uploadFile(inputStream, fileName, "text/csv");

            logger.info("错误文件生成成功: errorFileUrl={}", errorFileUrl);
            return errorFileUrl;
            
        } catch (Exception e) {
            logger.error("生成错误文件失败", e);
            throw new RuntimeException("生成错误文件失败", e);
        }
    }

    /**
     * 构建错误行数组
     */
    private <T> String[] buildErrorRow(ExcelRowError<T> error, int originalColumnCount) {
        String[] row = new String[originalColumnCount + 2];
        row[0] = String.valueOf(error.getRowIndex());
        row[1] = error.getErrorMessage();
        
        // 添加原始数据（简化处理，实际应该根据反射获取字段值）
        // 这里留空，实际使用时需要根据具体模型处理
        
        return row;
    }

    /**
     * 构建错误文件表头
     */
    private String[] buildErrorHeaders(String[] originalHeaders) {
        String[] headers = new String[originalHeaders.length + 2];
        headers[0] = "行号";
        headers[1] = "错误信息";
        System.arraycopy(originalHeaders, 0, headers, 2, originalHeaders.length);
        return headers;
    }

    /**
     * 将字符串数组列表转换为对象列表（用于 CsvUtils）
     */
    private List<Object> convertToObjectList(List<String[]> data) {
        return new ArrayList<>(data);
    }

    /**
     * 创建导入任务
     */
    private ImportTaskHistory createTask(String businessType, String fileUrl, String createdBy) {
        ImportTaskHistory task = new ImportTaskHistory();
        task.setBusinessType(businessType);
        task.setTaskStatus("PROCESSING");
        task.setOriginalFileUrl(fileUrl);
        task.setCreatedBy(createdBy);
        task.setCreatedTime(LocalDateTime.now());
        task.setUpdatedTime(LocalDateTime.now());
        
        importTaskHistoryMapper.insert(task);
        logger.info("创建CSV导入任务: taskId={}, businessType={}", task.getId(), businessType);
        
        return task;
    }

    /**
     * 更新任务为成功
     */
    private void updateTaskSuccess(Long taskId, ImportResult result) {
        ImportTaskHistory task = new ImportTaskHistory();
        task.setId(taskId);
        task.setTaskStatus("SUCCESS");
        task.setTotalRows(result.getTotalRows());
        task.setSuccessRows(result.getSuccessRows());
        task.setErrorRows(result.getErrorRows());
        task.setErrorFileUrl(result.getErrorFileUrl());
        task.setUpdatedTime(LocalDateTime.now());
        
        importTaskHistoryMapper.updateById(task);
    }

    /**
     * 更新任务为失败
     */
    private void updateTaskFailed(Long taskId, String errorMessage) {
        ImportTaskHistory task = new ImportTaskHistory();
        task.setId(taskId);
        task.setTaskStatus("FAILED");
        task.setErrorMessage(errorMessage);
        task.setUpdatedTime(LocalDateTime.now());
        
        importTaskHistoryMapper.updateById(task);
    }
}
