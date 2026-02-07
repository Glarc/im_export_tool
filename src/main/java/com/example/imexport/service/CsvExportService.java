package com.example.imexport.service;

import com.example.imexport.core.CsvExportProvider;
import com.example.imexport.mapper.ExportTaskHistoryMapper;
import com.example.imexport.model.ExportTaskHistory;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CSV 导出服务核心类
 * 提供通用的 CSV 导出流程：查询数据 -> 生成 CSV -> 上传 OSS -> 更新任务
 */
@Service
public class CsvExportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvExportService.class);

    @Autowired
    private ExportTaskHistoryMapper exportTaskHistoryMapper;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 异步执行导出任务
     *
     * @param provider 数据提供者
     * @param params 查询参数
     * @param createdBy 创建人
     * @param <T> 导出数据模型类型
     * @return 任务ID
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public <T> Long executeExportAsync(CsvExportProvider<T> provider, Object params, String createdBy) {
        // 创建任务记录
        ExportTaskHistory task = createTask(provider.getBusinessType(), params, createdBy);
        
        try {
            // 执行导出
            String fileUrl = doExport(provider, params);
            
            // 更新任务状态
            updateTaskSuccess(task.getId(), fileUrl, 0);
            
            logger.info("CSV导出任务完成: taskId={}, fileUrl={}", task.getId(), fileUrl);
            return task.getId();
            
        } catch (Exception e) {
            logger.error("CSV导出任务失败: taskId={}", task.getId(), e);
            updateTaskFailed(task.getId(), e.getMessage());
            throw new RuntimeException("CSV导出任务失败", e);
        }
    }

    /**
     * 同步执行导出（用于测试或小数据量）
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> String executeExportSync(CsvExportProvider<T> provider, Object params, String createdBy) {
        ExportTaskHistory task = createTask(provider.getBusinessType(), params, createdBy);
        
        try {
            String fileUrl = doExport(provider, params);
            updateTaskSuccess(task.getId(), fileUrl, 0);
            return fileUrl;
        } catch (Exception e) {
            logger.error("CSV导出任务失败: taskId={}", task.getId(), e);
            updateTaskFailed(task.getId(), e.getMessage());
            throw new RuntimeException("CSV导出任务失败", e);
        }
    }

    /**
     * 核心导出逻辑
     */
    private <T> String doExport(CsvExportProvider<T> provider, Object params) {
        // 查询数据
        List<T> data = provider.queryExportData(params);
        
        if (data == null || data.isEmpty()) {
            logger.warn("导出数据为空: businessType={}", provider.getBusinessType());
        }

        // 生成 CSV
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            CsvUtils.writeCsv(outputStream, data, provider.getCsvHeaders());
        } catch (Exception e) {
            logger.error("生成 CSV 失败", e);
            throw new RuntimeException("生成 CSV 失败", e);
        }

        // 上传到 OSS
        String fileName = provider.getExportFileName() + "_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        String fileUrl = fileStorageService.uploadFile(inputStream, fileName, "text/csv");

        logger.info("CSV导出文件生成成功: fileUrl={}, rows={}", fileUrl, data != null ? data.size() : 0);
        return fileUrl;
    }

    /**
     * 创建导出任务
     */
    private ExportTaskHistory createTask(String businessType, Object params, String createdBy) {
        ExportTaskHistory task = new ExportTaskHistory();
        task.setBusinessType(businessType);
        task.setTaskStatus("PROCESSING");
        task.setQueryParams(params != null ? params.toString() : "");
        task.setCreatedBy(createdBy);
        task.setCreatedTime(LocalDateTime.now());
        task.setUpdatedTime(LocalDateTime.now());
        
        exportTaskHistoryMapper.insert(task);
        logger.info("创建CSV导出任务: taskId={}, businessType={}", task.getId(), businessType);
        
        return task;
    }

    /**
     * 更新任务为成功
     */
    private void updateTaskSuccess(Long taskId, String fileUrl, int totalRows) {
        ExportTaskHistory task = new ExportTaskHistory();
        task.setId(taskId);
        task.setTaskStatus("SUCCESS");
        task.setFileUrl(fileUrl);
        task.setTotalRows(totalRows);
        task.setUpdatedTime(LocalDateTime.now());
        
        exportTaskHistoryMapper.updateById(task);
    }

    /**
     * 更新任务为失败
     */
    private void updateTaskFailed(Long taskId, String errorMessage) {
        ExportTaskHistory task = new ExportTaskHistory();
        task.setId(taskId);
        task.setTaskStatus("FAILED");
        task.setErrorMessage(errorMessage);
        task.setUpdatedTime(LocalDateTime.now());
        
        exportTaskHistoryMapper.updateById(task);
    }
}
