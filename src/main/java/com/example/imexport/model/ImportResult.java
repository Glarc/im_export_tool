package com.example.imexport.model;

import lombok.Data;

/**
 * 导入结果
 */
@Data
public class ImportResult {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 总行数
     */
    private int totalRows;

    /**
     * 成功行数
     */
    private int successRows;

    /**
     * 错误行数
     */
    private int errorRows;

    /**
     * 错误文件 OSS 地址
     */
    private String errorFileUrl;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String message;
}
