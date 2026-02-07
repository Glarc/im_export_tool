package com.example.imexport.core;

/**
 * Excel 模板提供者接口
 * 业务层实现此接口以提供模板下载功能
 *
 * @param <T> Excel 数据模型类型
 */
public interface ExcelTemplateProvider<T> extends TemplateProvider<T> {

    /**
     * 获取 Excel 数据模型类（用于生成模板）
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
