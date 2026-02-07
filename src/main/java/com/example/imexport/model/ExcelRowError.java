package com.example.imexport.model;

import lombok.Data;

/**
 * 带错误信息的 Excel 行数据包装类
 */
@Data
public class ExcelRowError<T> {
    
    /**
     * 行号（从1开始，不含表头）
     */
    private int rowIndex;

    /**
     * 行数据
     */
    private T rowData;

    /**
     * 错误信息
     */
    private String errorMessage;

    public ExcelRowError(int rowIndex, T rowData, String errorMessage) {
        this.rowIndex = rowIndex;
        this.rowData = rowData;
        this.errorMessage = errorMessage;
    }
}
