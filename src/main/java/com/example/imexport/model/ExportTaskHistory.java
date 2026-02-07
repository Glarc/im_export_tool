package com.example.imexport.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 导出任务历史表
 */
@Data
@TableName("export_task_history")
public class ExportTaskHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务类型标识
     */
    private String businessType;

    /**
     * 任务状态：PENDING, PROCESSING, SUCCESS, FAILED
     */
    private String taskStatus;

    /**
     * 导出文件 OSS 地址
     */
    private String fileUrl;

    /**
     * 导出数据行数
     */
    private Integer totalRows;

    /**
     * 查询参数（JSON 格式）
     */
    private String queryParams;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建人
     */
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
