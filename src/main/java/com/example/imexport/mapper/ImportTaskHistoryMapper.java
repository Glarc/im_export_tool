package com.example.imexport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.imexport.model.ImportTaskHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 导入任务历史 Mapper
 */
@Mapper
public interface ImportTaskHistoryMapper extends BaseMapper<ImportTaskHistory> {
}
