-- 导入任务历史表
CREATE TABLE IF NOT EXISTS `import_task_history` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型标识',
  `task_status` VARCHAR(20) NOT NULL COMMENT '任务状态：PENDING, PROCESSING, SUCCESS, FAILED',
  `original_file_url` VARCHAR(500) COMMENT '原始文件OSS地址',
  `total_rows` INT(11) COMMENT '总行数',
  `success_rows` INT(11) COMMENT '成功行数',
  `error_rows` INT(11) COMMENT '错误行数',
  `error_file_url` VARCHAR(500) COMMENT '错误文件OSS地址',
  `error_message` TEXT COMMENT '错误信息',
  `created_by` VARCHAR(50) COMMENT '创建人',
  `created_time` DATETIME COMMENT '创建时间',
  `updated_time` DATETIME COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_business_type` (`business_type`),
  KEY `idx_task_status` (`task_status`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导入任务历史表';

-- 导出任务历史表
CREATE TABLE IF NOT EXISTS `export_task_history` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型标识',
  `task_status` VARCHAR(20) NOT NULL COMMENT '任务状态：PENDING, PROCESSING, SUCCESS, FAILED',
  `file_url` VARCHAR(500) COMMENT '导出文件OSS地址',
  `total_rows` INT(11) COMMENT '导出数据行数',
  `query_params` TEXT COMMENT '查询参数（JSON格式）',
  `error_message` TEXT COMMENT '错误信息',
  `created_by` VARCHAR(50) COMMENT '创建人',
  `created_time` DATETIME COMMENT '创建时间',
  `updated_time` DATETIME COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_business_type` (`business_type`),
  KEY `idx_task_status` (`task_status`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导出任务历史表';
