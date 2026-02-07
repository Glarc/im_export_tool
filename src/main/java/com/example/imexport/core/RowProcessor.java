package com.example.imexport.core;

import java.util.List;

/**
 * 通用行数据处理器接口
 * 业务层实现此接口以定义行级校验和处理逻辑，与文件格式无关
 *
 * @param <T> 行数据模型类型
 */
public interface RowProcessor<T> {

    /**
     * 校验单行数据
     *
     * @param rowData 行数据
     * @param rowIndex 行号（从1开始，不含表头）
     * @return 校验结果，null或空字符串表示校验通过
     */
    String validateRow(T rowData, int rowIndex);

    /**
     * 处理有效数据行（批量保存）
     *
     * @param validRows 所有校验通过的数据行
     */
    void processValidRows(List<T> validRows);

    /**
     * 获取业务标识
     *
     * @return 业务类型标识
     */
    String getBusinessType();

    /**
     * 获取数据模型类
     *
     * @return 数据模型类
     */
    Class<T> getModelClass();
}
