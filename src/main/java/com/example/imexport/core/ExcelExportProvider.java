package com.example.imexport.core;

import java.util.List;

/**
 * Excel 导出数据提供者接口
 * 业务层实现此接口以提供导出数据
 *
 * @param <T> 导出数据模型类型
 */
public interface ExcelExportProvider<T> {

    /**
     * 查询需要导出的数据
     *
     * @param params 查询参数（业务自定义）
     * @return 导出数据列表
     */
    List<T> queryExportData(Object params);

    /**
     * 获取业务标识
     *
     * @return 业务类型标识
     */
    String getBusinessType();

    /**
     * 获取 Excel 数据模型类
     *
     * @return 数据模型类
     */
    Class<T> getExcelModelClass();

    /**
     * 获取导出文件名（不含扩展名）
     *
     * @return 文件名
     */
    String getExportFileName();
}
