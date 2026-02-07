package com.example.imexport.storage;

import java.io.InputStream;

/**
 * 文件存储服务接口（OSS 抽象层）
 * 业务统一通过此接口上传下载文件，底层对接 DfsServiceStorageClient
 */
public interface FileStorageService {

    /**
     * 上传文件到 OSS
     *
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param contentType 内容类型
     * @return OSS 文件地址
     */
    String uploadFile(InputStream inputStream, String fileName, String contentType);

    /**
     * 从 OSS 下载文件
     *
     * @param fileUrl OSS 文件地址
     * @return 文件输入流
     */
    InputStream downloadFile(String fileUrl);

    /**
     * 生成临时访问 URL
     *
     * @param fileUrl OSS 文件地址
     * @param expirationSeconds 过期时间（秒）
     * @return 临时访问 URL
     */
    String generatePresignedUrl(String fileUrl, long expirationSeconds);
}
