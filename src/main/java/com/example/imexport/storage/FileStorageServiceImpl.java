package com.example.imexport.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * 文件存储服务实现类（模拟 DfsServiceStorageClient）
 * 实际项目中应对接真实的 DfsServiceStorageClient
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    // 实际项目中注入: @Autowired private DfsServiceStorageClient dfsClient;

    @Override
    public String uploadFile(InputStream inputStream, String fileName, String contentType) {
        try {
            // 实际代码：String fileUrl = dfsClient.upload(inputStream, fileName, contentType);
            
            // 模拟上传逻辑
            String fileId = UUID.randomUUID().toString();
            String fileUrl = "oss://bucket/" + fileId + "/" + fileName;
            
            logger.info("文件上传成功: fileName={}, fileUrl={}", fileName, fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            logger.error("文件上传失败: fileName={}", fileName, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream downloadFile(String fileUrl) {
        try {
            // 实际代码：return dfsClient.download(fileUrl);
            
            // 模拟下载逻辑
            logger.info("文件下载: fileUrl={}", fileUrl);
            
            // 返回模拟数据
            return new ByteArrayInputStream("mock file content".getBytes());
            
        } catch (Exception e) {
            logger.error("文件下载失败: fileUrl={}", fileUrl, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUrl(String fileUrl, long expirationSeconds) {
        try {
            // 实际代码：return dfsClient.generatePresignedUrl(fileUrl, expirationSeconds);
            
            // 模拟生成临时URL
            String presignedUrl = "https://oss.example.com" + fileUrl.replace("oss://", "/") 
                + "?expires=" + expirationSeconds;
            
            logger.info("生成临时URL: fileUrl={}, presignedUrl={}", fileUrl, presignedUrl);
            return presignedUrl;
            
        } catch (Exception e) {
            logger.error("生成临时URL失败: fileUrl={}", fileUrl, e);
            throw new RuntimeException("生成临时URL失败: " + e.getMessage(), e);
        }
    }
}
