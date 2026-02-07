package com.example.imexport.core;

/**
 * Excel 模板提供者接口
 * 业务层实现此接口以提供模板下载功能
 *
 * @param <T> Excel 数据模型类型
 */
public interface ExcelTemplateProvider<T> {

    /**
     * 获取业务标识
     *
     * @return 业务类型标识
     */
    String getBusinessType();

    /**
     * 获取 Excel 数据模型类（用于生成模板）
     *
     * @return 数据模型类
     */
    Class<T> getExcelModelClass();

    /**
     * 获取模板文件名（不含扩展名）
     *
     * @return 文件名
     */
    String getTemplateFileName();
}
