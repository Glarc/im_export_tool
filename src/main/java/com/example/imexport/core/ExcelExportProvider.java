package com.example.imexport.core;

/**
 * Excel 导出数据提供者接口
 * 业务层实现此接口以提供导出数据
 *
 * @param <T> 导出数据模型类型
 */
public interface ExcelExportProvider<T> extends DataProvider<T> {

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
