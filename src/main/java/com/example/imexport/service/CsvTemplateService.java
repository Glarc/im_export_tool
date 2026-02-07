package com.example.imexport.service;

import com.example.imexport.core.CsvTemplateProvider;
import com.example.imexport.storage.FileStorageService;
import com.example.imexport.util.CsvUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * CSV 模板服务
 * 提供 CSV 模板生成和下载功能
 */
@Service
public class CsvTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(CsvTemplateService.class);

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 生成模板并上传到 OSS，返回下载地址
     *
     * @param provider 模板提供者
     * @param <T> 数据模型类型
     * @return 模板文件下载地址
     */
    public <T> String generateTemplateDownloadUrl(CsvTemplateProvider<T> provider) {
        try {
            // 生成空白 CSV（仅包含表头）
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CsvUtils.writeCsv(outputStream, new ArrayList<>(), provider.getCsvHeaders());

            // 上传到 OSS
            String fileName = provider.getTemplateFileName() + "_template_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
            
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            String templateUrl = fileStorageService.uploadFile(inputStream, fileName, "text/csv");

            logger.info("CSV模板生成成功: businessType={}, templateUrl={}", 
                provider.getBusinessType(), templateUrl);
            
            return templateUrl;
            
        } catch (Exception e) {
            logger.error("生成CSV模板失败: businessType={}", provider.getBusinessType(), e);
            throw new RuntimeException("生成CSV模板失败", e);
        }
    }
}
