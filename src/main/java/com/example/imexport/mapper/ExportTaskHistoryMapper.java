package com.example.imexport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.imexport.model.ExportTaskHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 导出任务历史 Mapper
 */
@Mapper
public interface ExportTaskHistoryMapper extends BaseMapper<ExportTaskHistory> {
}
