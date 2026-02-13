-- 知识库表结构
-- 用于存储AI预测的历史经验和统计数据

-- 比赛知识表
CREATE TABLE IF NOT EXISTS `match_knowledge` (
    `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
    `match_id` VARCHAR(64) NOT NULL COMMENT '关联的比赛ID',
    `league` VARCHAR(50) COMMENT '联赛名称',
    `home_team` VARCHAR(100) NOT NULL COMMENT '主队名称',
    `away_team` VARCHAR(100) NOT NULL COMMENT '客队名称',
    `match_time` DATETIME COMMENT '比赛时间',
    `odds_snapshot` VARCHAR(200) COMMENT '赛前赔率快照 JSON格式',
    `ai_prediction` VARCHAR(20) COMMENT 'AI预测结果 (主胜/平局/客胜)',
    `ai_score` VARCHAR(20) COMMENT 'AI预测比分',
    `confidence` INT COMMENT 'AI预测置信度 (0-100)',
    `actual_result` VARCHAR(20) COMMENT '实际比赛结果',
    `actual_score` VARCHAR(20) COMMENT '实际比分',
    `prediction_correct` TINYINT(1) COMMENT '胜负预测是否正确',
    `score_correct` TINYINT(1) COMMENT '比分预测是否正确',
    `key_features` TEXT COMMENT '关键分析特征 JSON格式',
    `analysis_summary` TEXT COMMENT '分析摘要 - 用于向量检索',
    `learning_insight` TEXT COMMENT '学习经验 - 从赛后复盘中提取',
    `scenario_tags` VARCHAR(200) COMMENT '场景标签 (如: 强队对弱队、德比战、保级战等)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_match_id` (`match_id`),
    KEY `idx_league` (`league`),
    KEY `idx_teams` (`home_team`, `away_team`),
    KEY `idx_prediction_correct` (`prediction_correct`),
    KEY `idx_match_time` (`match_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛知识库';

-- 预测统计表
CREATE TABLE IF NOT EXISTS `prediction_stats` (
    `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
    `stats_date` DATE NOT NULL COMMENT '统计日期',
    `league` VARCHAR(50) COMMENT '联赛名称 (null表示全部联赛)',
    `total_predictions` INT DEFAULT 0 COMMENT '总预测场次',
    `correct_predictions` INT DEFAULT 0 COMMENT '胜负预测正确数',
    `correct_scores` INT DEFAULT 0 COMMENT '比分预测正确数',
    `high_confidence_predictions` INT DEFAULT 0 COMMENT '高置信度预测数 (置信度>=70)',
    `high_confidence_correct` INT DEFAULT 0 COMMENT '高置信度正确数',
    `upset_correct` INT DEFAULT 0 COMMENT '冷门预测成功数',
    `upset_total` INT DEFAULT 0 COMMENT '冷门预测总数',
    `result_accuracy` DECIMAL(5,2) COMMENT '胜负预测准确率',
    `score_accuracy` DECIMAL(5,2) COMMENT '比分预测准确率',
    `high_confidence_accuracy` DECIMAL(5,2) COMMENT '高置信度准确率',
    `upset_accuracy` DECIMAL(5,2) COMMENT '冷门预测准确率',
    `roi` DECIMAL(8,2) COMMENT 'ROI (投资回报率模拟)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_date_league` (`stats_date`, `league`),
    KEY `idx_stats_date` (`stats_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预测统计表';

-- 在 ai_analysis_result 表添加置信度字段 (如果不存在)
ALTER TABLE `ai_analysis_result`
ADD COLUMN IF NOT EXISTS `confidence` INT COMMENT 'AI预测置信度 (0-100)' AFTER `ai_result`;

-- 添加联赛字段 (如果不存在)
ALTER TABLE `ai_analysis_result`
ADD COLUMN IF NOT EXISTS `league` VARCHAR(50) COMMENT '联赛名称' AFTER `match_id`;
