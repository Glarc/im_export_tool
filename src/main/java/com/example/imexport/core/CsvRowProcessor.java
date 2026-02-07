package com.example.imexport.core;

/**
 * CSV 行数据处理器接口
 * 业务层实现此接口以定义 CSV 导入的行级校验和处理逻辑
 *
 * @param <T> CSV 行数据模型类型
 */
public interface CsvRowProcessor<T> extends RowProcessor<T> {

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
     * 获取 CSV 列名数组（用于解析和生成）
     * 必须按照数据模型字段顺序定义
     *
     * @return 列名数组
     */
    String[] getCsvHeaders();
}
