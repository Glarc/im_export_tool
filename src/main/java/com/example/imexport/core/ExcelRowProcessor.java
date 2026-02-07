package com.example.imexport.core;

/**
 * Excel 行数据处理器接口
 * 业务层实现此接口以定义行级校验和处理逻辑
 *
 * @param <T> Excel 行数据模型类型
 */
public interface ExcelRowProcessor<T> extends RowProcessor<T> {

    /**
     * 获取 Excel 数据模型类
     *
     * @return 数据模型类
     */
    Class<T> getExcelModelClass();
    
    /**
     * 默认实现，委托给 getExcelModelClass()
     */
    @Override
    default Class<T> getModelClass() {
        return getExcelModelClass();
    }
}
