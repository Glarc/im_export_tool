package com.example.imexport.service;

import com.alibaba.excel.EasyExcel;
import com.example.imexport.core.ExcelTemplateProvider;
import com.example.imexport.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Excel 模板服务
 * 提供模板生成和下载功能
 */
@Service
public class ExcelTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelTemplateService.class);

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 生成并上传模板文件
     *
     * @param provider 模板提供者
     * @param <T> Excel 数据模型类型
     * @return 模板文件 OSS 地址
     */
    public <T> String generateTemplate(ExcelTemplateProvider<T> provider) {
        try {
            // 生成空白 Excel 模板（仅包含表头）
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            EasyExcel.write(outputStream, provider.getExcelModelClass())
                .sheet(provider.getBusinessType())
                .doWrite(new ArrayList<>()); // 空数据，仅生成表头

            // 上传到 OSS
            String fileName = provider.getTemplateFileName() + "_template.xlsx";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            String templateUrl = fileStorageService.uploadFile(inputStream, fileName, 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            logger.info("模板文件生成成功: businessType={}, templateUrl={}", 
                provider.getBusinessType(), templateUrl);
            
            return templateUrl;
            
        } catch (Exception e) {
            logger.error("模板文件生成失败: businessType={}", provider.getBusinessType(), e);
            throw new RuntimeException("模板文件生成失败", e);
        }
    }

    /**
     * 生成临时下载链接
     *
     * @param provider 模板提供者
     * @param <T> Excel 数据模型类型
     * @return 临时下载 URL
     */
    public <T> String generateTemplateDownloadUrl(ExcelTemplateProvider<T> provider) {
        String templateUrl = generateTemplate(provider);
        return fileStorageService.generatePresignedUrl(templateUrl, 3600); // 1小时有效期
    }
}
