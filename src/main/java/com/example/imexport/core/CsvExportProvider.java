package com.example.imexport.core;

/**
 * CSV 导出数据提供者接口
 * 业务层实现此接口以提供 CSV 导出数据
 *
 * @param <T> 导出数据模型类型
 */
public interface CsvExportProvider<T> extends DataProvider<T> {

    /**
     * 获取 CSV 数据模型类
     *
     * @return 数据模型类
     */
    Class<T> getCsvModelClass();
    
    /**
     * 默认实现，委托给 getCsvModelClass()
     */
    @Override
    default Class<T> getModelClass() {
        return getCsvModelClass();
    }
    
    /**
     * 获取 CSV 列名数组（用于生成表头）
     * 必须按照数据模型字段顺序定义
     *
     * @return 列名数组
     */
    String[] getCsvHeaders();
}
