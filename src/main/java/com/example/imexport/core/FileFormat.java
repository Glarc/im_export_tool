package com.example.imexport.core;

/**
 * 文件格式枚举
 * 定义支持的文件格式类型
 */
public enum FileFormat {
    /**
     * Excel 格式 (.xlsx)
     */
    EXCEL("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    
    /**
     * CSV 格式 (.csv)
     */
    CSV("csv", "text/csv");
    
    private final String extension;
    private final String contentType;
    
    FileFormat(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }
    
    public String getExtension() {
        return extension;
    }
    
    public String getContentType() {
        return contentType;
    }
}
