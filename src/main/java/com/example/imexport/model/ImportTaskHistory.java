package com.example.imexport.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 导入任务历史表
 */
@Data
@TableName("import_task_history")
public class ImportTaskHistory {

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
     * 原始文件 OSS 地址
     */
    private String originalFileUrl;

    /**
     * 总行数（不含表头）
     */
    private Integer totalRows;

    /**
     * 成功行数
     */
    private Integer successRows;

    /**
     * 错误行数
     */
    private Integer errorRows;

    /**
     * 错误文件 OSS 地址
     */
    private String errorFileUrl;

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
