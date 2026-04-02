-- AI预测模型统计表（简化版）
-- 统计逻辑在Java代码中实现

-- 预测模型表
CREATE TABLE IF NOT EXISTS `prediction_model` (
    `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
    `model_name` VARCHAR(100) NOT NULL COMMENT '模型名称',
    `model_type` VARCHAR(20) NOT NULL COMMENT '模型类型 (result:胜负预测, score:比分预测)',
    `model_version` VARCHAR(20) NOT NULL COMMENT '模型版本',
    `description` VARCHAR(500) COMMENT '模型描述',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用 (1:启用, 0:禁用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name_version` (`model_name`, `model_version`),
    KEY `idx_model_type` (`model_type`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预测模型表';

-- 预测模型准确率统计表
CREATE TABLE IF NOT EXISTS `prediction_model_stats` (
    `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
    `model_id` VARCHAR(32) NOT NULL COMMENT '模型ID',
    `model_name` VARCHAR(100) NOT NULL COMMENT '模型名称',
    `model_type` VARCHAR(20) NOT NULL COMMENT '模型类型 (result:胜负预测, score:比分预测)',
    `stats_date` DATETIME NOT NULL COMMENT '统计时间',
    `sample_size` INT NOT NULL DEFAULT 30 COMMENT '样本数量',
    `total_predictions` INT DEFAULT 0 COMMENT '总预测场次',
    `correct_predictions` INT DEFAULT 0 COMMENT '正确预测数',
    `accuracy_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '准确率 (%)',
    `home_win_count` INT DEFAULT 0 COMMENT '主胜预测数',
    `home_win_correct` INT DEFAULT 0 COMMENT '主胜预测正确数',
    `draw_count` INT DEFAULT 0 COMMENT '平局预测数',
    `draw_correct` INT DEFAULT 0 COMMENT '平局预测正确数',
    `away_win_count` INT DEFAULT 0 COMMENT '客胜预测数',
    `away_win_correct` INT DEFAULT 0 COMMENT '客胜预测正确数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_model_id` (`model_id`),
    KEY `idx_model_name` (`model_name`),
    KEY `idx_stats_date` (`stats_date`),
    KEY `idx_model_type` (`model_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预测模型准确率统计表';

-- 初始化模型数据
INSERT INTO `prediction_model` (`id`, `model_name`, `model_type`, `model_version`, `description`, `is_active`)
VALUES
('MODEL_RESULT_V1', '胜负模型v1', 'result', 'v1', '基于AI分析的胜平负预测模型', 1),
('MODEL_SCORE_V1', '比分模型v1', 'score', 'v1', '基于AI分析的比分预测模型', 1)
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    is_active = VALUES(is_active),
    update_time = CURRENT_TIMESTAMP;
