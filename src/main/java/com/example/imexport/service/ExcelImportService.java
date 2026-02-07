package com.example.imexport.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.imexport.core.ExcelRowProcessor;
import com.example.imexport.mapper.ImportTaskHistoryMapper;
import com.example.imexport.model.ExcelRowError;
import com.example.imexport.model.ImportResult;
import com.example.imexport.model.ImportTaskHistory;
import com.example.imexport.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 导入服务核心类
 * 提供通用的 Excel 导入流程：解析 -> 校验 -> 收集错误 -> 保存有效数据 -> 生成错误文件
 */
@Service
public class ExcelImportService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelImportService.class);

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
     * @param <T> Excel 数据模型类型
     * @return 任务ID
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public <T> Long executeImportAsync(String fileUrl, ExcelRowProcessor<T> processor, String createdBy) {
        // 创建任务记录
        ImportTaskHistory task = createTask(processor.getBusinessType(), fileUrl, createdBy);
        
        try {
            // 执行导入
            ImportResult result = doImport(fileUrl, processor, task.getId());
            
            // 更新任务状态
            updateTaskSuccess(task.getId(), result);
            
            logger.info("导入任务完成: taskId={}, result={}", task.getId(), result);
            return task.getId();
            
        } catch (Exception e) {
            logger.error("导入任务失败: taskId={}", task.getId(), e);
            updateTaskFailed(task.getId(), e.getMessage());
            throw new RuntimeException("导入任务失败", e);
        }
    }

    /**
     * 同步执行导入（用于测试或小数据量）
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> ImportResult executeImportSync(String fileUrl, ExcelRowProcessor<T> processor, String createdBy) {
        ImportTaskHistory task = createTask(processor.getBusinessType(), fileUrl, createdBy);
        
        try {
            ImportResult result = doImport(fileUrl, processor, task.getId());
            updateTaskSuccess(task.getId(), result);
            return result;
        } catch (Exception e) {
            logger.error("导入任务失败: taskId={}", task.getId(), e);
            updateTaskFailed(task.getId(), e.getMessage());
            throw new RuntimeException("导入任务失败", e);
        }
    }

    /**
     * 核心导入逻辑
     */
    private <T> ImportResult doImport(String fileUrl, ExcelRowProcessor<T> processor, Long taskId) {
        ImportResult result = new ImportResult();
        result.setTaskId(taskId);

        List<T> validRows = new ArrayList<>();
        List<ExcelRowError<T>> errorRows = new ArrayList<>();

        // 下载文件
        InputStream inputStream = fileStorageService.downloadFile(fileUrl);

        // 使用 EasyExcel 解析
        EasyExcel.read(inputStream, processor.getExcelModelClass(), new AnalysisEventListener<T>() {
            private int rowIndex = 0;

            @Override
            public void invoke(T data, AnalysisContext context) {
                rowIndex++;
                
                // 校验数据
                String errorMsg = processor.validateRow(data, rowIndex);
                
                if (errorMsg == null || errorMsg.isEmpty()) {
                    validRows.add(data);
                } else {
                    errorRows.add(new ExcelRowError<>(rowIndex, data, errorMsg));
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                logger.info("Excel 解析完成: totalRows={}, validRows={}, errorRows={}", 
                    rowIndex, validRows.size(), errorRows.size());
            }
        }).sheet().doRead();

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
     * 生成错误 Excel 文件
     */
    private <T> String generateErrorFile(List<ExcelRowError<T>> errorRows, ExcelRowProcessor<T> processor) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // 创建包含错误信息的数据模型
            List<ErrorExcelRow<T>> errorExcelRows = new ArrayList<>();
            for (ExcelRowError<T> error : errorRows) {
                ErrorExcelRow<T> errorExcelRow = new ErrorExcelRow<>();
                errorExcelRow.setRowIndex(error.getRowIndex());
                errorExcelRow.setErrorMessage(error.getErrorMessage());
                errorExcelRow.setRowData(error.getRowData());
                errorExcelRows.add(errorExcelRow);
            }

            // 写入 Excel
            EasyExcel.write(outputStream, ErrorExcelRow.class)
                .sheet("错误数据")
                .doWrite(errorExcelRows);

            // 上传到 OSS
            String fileName = "error_" + processor.getBusinessType() + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
            
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            String errorFileUrl = fileStorageService.uploadFile(inputStream, fileName, 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            logger.info("错误文件生成成功: errorFileUrl={}", errorFileUrl);
            return errorFileUrl;
            
        } catch (Exception e) {
            logger.error("生成错误文件失败", e);
            throw new RuntimeException("生成错误文件失败", e);
        }
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
        logger.info("创建导入任务: taskId={}, businessType={}", task.getId(), businessType);
        
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

    /**
     * 错误行数据模型（用于生成错误 Excel）
     */
    @lombok.Data
    public static class ErrorExcelRow<T> {
        @com.alibaba.excel.annotation.ExcelProperty("行号")
        private int rowIndex;

        @com.alibaba.excel.annotation.ExcelProperty("错误信息")
        private String errorMessage;

        // 原始数据（需要业务自行扩展具体字段）
        private T rowData;
    }
}
