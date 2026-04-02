-- AI预测模型统计表
-- 用于统计不同预测模型的准确率

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
    `sample_size` INT NOT NULL DEFAULT 100 COMMENT '样本数量 (默认近100场)',
    `total_predictions` INT DEFAULT 0 COMMENT '总预测场次',
    `correct_predictions` INT DEFAULT 0 COMMENT '正确预测数',
    `accuracy_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '准确率 (%)',
    `home_win_count` INT DEFAULT 0 COMMENT '主胜预测数',
    `home_win_correct` INT DEFAULT 0 COMMENT '主胜预测正确数',
    `draw_count` INT DEFAULT 0 COMMENT '平局预测数',
    `draw_correct` INT DEFAULT 0 COMMENT '平局预测正确数',
    `away_win_count` INT DEFAULT 0 COMMENT '客胜预测数',
    `away_win_correct` INT DEFAULT 0 COMMENT '客胜预测正确数',
    `recent_10_accuracy` DECIMAL(5,2) COMMENT '近10场准确率',
    `recent_20_accuracy` DECIMAL(5,2) COMMENT '近20场准确率',
    `recent_50_accuracy` DECIMAL(5,2) COMMENT '近50场准确率',
    `league_stats` TEXT COMMENT '分联赛统计 JSON格式',
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
('MODEL_RESULT_V1', '胜负模型v1', 'result', 'v1', '基于AI分析的胜平负预测模型，统计近100场比赛的胜负预测准确率', 1),
('MODEL_SCORE_V1', '比分模型v1', 'score', 'v1', '基于AI分析的比分预测模型，统计近100场比赛的比分预测准确率', 1);

-- 创建统计计算的存储过程 - 胜负模型
DELIMITER $$

CREATE PROCEDURE `calculate_result_model_accuracy`()
BEGIN
    DECLARE v_model_id VARCHAR(32);
    DECLARE v_total INT;
    DECLARE v_correct INT;
    DECLARE v_accuracy DECIMAL(5,2);
    DECLARE v_home_win_count INT;
    DECLARE v_home_win_correct INT;
    DECLARE v_draw_count INT;
    DECLARE v_draw_correct INT;
    DECLARE v_away_win_count INT;
    DECLARE v_away_win_correct INT;
    DECLARE v_recent_10 DECIMAL(5,2);
    DECLARE v_recent_20 DECIMAL(5,2);
    DECLARE v_recent_50 DECIMAL(5,2);

    SET v_model_id = 'MODEL_RESULT_V1';

    -- 计算近100场胜负预测准确率
    -- 首先需要从match_result中解析出胜平负结果
    SELECT
        COUNT(*) INTO v_total
    FROM (
        SELECT * FROM ai_analysis_result
        WHERE match_result IS NOT NULL
        AND ai_result IS NOT NULL
        ORDER BY match_time DESC
        LIMIT 100
    ) t;

    -- 计算正确预测数
    -- 逻辑：比较ai_result和从match_result解析的实际结果
    SELECT
        COUNT(*) INTO v_correct
    FROM (
        SELECT
            ai_result,
            match_result,
            CASE
                WHEN CAST(SUBSTRING_INDEX(match_result, ':', 1) AS SIGNED) >
                     CAST(SUBSTRING_INDEX(match_result, ':', -1) AS SIGNED) THEN '胜'
                WHEN CAST(SUBSTRING_INDEX(match_result, ':', 1) AS SIGNED) =
                     CAST(SUBSTRING_INDEX(match_result, ':', -1) AS SIGNED) THEN '平'
                WHEN CAST(SUBSTRING_INDEX(match_result, ':', 1) AS SIGNED) <
                     CAST(SUBSTRING_INDEX(match_result, ':', -1) AS SIGNED) THEN '负'
            END AS actual_result
        FROM ai_analysis_result
        WHERE match_result IS NOT NULL
        AND ai_result IS NOT NULL
        AND match_result REGEXP '^[0-9]+:[0-9]+$'
        ORDER BY match_time DESC
        LIMIT 100
    ) t
    WHERE ai_result = actual_result;

    -- 计算准确率
    IF v_total > 0 THEN
        SET v_accuracy = (v_correct / v_total) * 100;
    ELSE
        SET v_accuracy = 0;
    END IF;

    -- 统计各类预测的数量和正确数
    SELECT
        SUM(CASE WHEN ai_result = '胜' THEN 1 ELSE 0 END),
        SUM(CASE WHEN ai_result = '胜' AND ai_result = actual_result THEN 1 ELSE 0 END),
        SUM(CASE WHEN ai_result = '平' THEN 1 ELSE 0 END),
        SUM(CASE WHEN ai_result = '平' AND ai_result = actual_result THEN 1 ELSE 0 END),
        SUM(CASE WHEN ai_result = '负' THEN 1 ELSE 0 END),
        SUM(CASE WHEN ai_result = '负' AND ai_result = actual_result THEN 1 ELSE 0 END)
    INTO
        v_home_win_count, v_home_win_correct,
        v_draw_count, v_draw_correct,
        v_away_win_count, v_away_win_correct
    FROM (
        SELECT
            ai_result,
            CASE
                WHEN CAST(SUBSTRING_INDEX(match_result, ':', 1) AS SIGNED) >
                     CAST(SUBSTRING_INDEX(match_result, ':', -1) AS SIGNED) THEN '胜'
                WHEN CAST(SUBSTRING_INDEX(match_result, ':', 1) AS SIGNED) =
                     CAST(SUBSTRING_INDEX(match_result, ':', -1) AS SIGNED) THEN '平'
                WHEN CAST(SUBSTRING_INDEX(match_result, ':', 1) AS SIGNED) <
                     CAST(SUBSTRING_INDEX(match_result, ':', -1) AS SIGNED) THEN '负'
            END AS actual_result
        FROM ai_analysis_result
        WHERE match_result IS NOT NULL
        AND ai_result IS NOT NULL
        AND match_result REGEXP '^[0-9]+:[0-9]+$'
        ORDER BY match_time DESC
        LIMIT 100
    ) t;

    -- 计算近10、20、50场准确率
    SELECT
        (SUM(CASE WHEN ai_result = actual_result THEN 1 ELSE 0 END) / COUNT(*)) * 100
    INTO v_recent_10
    FROM (
        SELECT
            ai_result,
            CASE
                WHEN CAST(SUBSTRING_INDEX(match_result, ':', 1) AS SIGNED) >
                     CAST(SUBSTRING_INDEX(match_result, ':', -1) AS SIGNED) THEN '胜'
                WHEN CAST(SUBSTRING_INDEX(match_result, ':', 1) AS SIGNED) =
                     CAST(SUBSTRING_INDEX(match_result, ':', -1) AS SIGNED) THEN '平'
                WHEN CAST(SUBSTRING_INDEX(match_result, ':', 1) AS SIGNED) <
                     CAST(SUBSTRING_INDEX(match_result, ':', -1) AS SIGNED) THEN '负'
            END AS actual_result
        FROM ai_analysis_result
        WHERE match_result IS NOT NULL
        AND ai_result IS NOT NULL
        AND match_result REGEXP '^[0-9]+:[0-9]+$'
        ORDER BY match_time DESC
        LIMIT 10
    ) t;

    SELECT
        (SUM(CASE WHEN ai_result = actual_result THEN 1 ELSE 0 END) / COUNT(*)) * 100
    INTO v_recent_20
    FROM (
        SELECT
            ai_result,
            CASE
                WHEN CAST(SUBSTRING_INDEX(match_result, ':', 1) AS SIGNED) >
                     CAST(SUBSTRING_INDEX(match_result, ':', -1) AS SIGNED) THEN '胜'
                WHEN CAST(SUBSTRING_INDEX(match_result, ':', 1) AS SIGNED) =
                     CAST(SUBSTRING_INDEX(match_result, ':', -1) AS SIGNED) THEN '平'
                WHEN CAST(SUBSTRING_INDEX(match_result, ':', 1) AS SIGNED) <
                     CAST(SUBSTRING_INDEX(match_result, ':', -1) AS SIGNED) THEN '负'
            END AS actual_result
        FROM ai_analysis_result
        WHERE match_result IS NOT NULL
        AND ai_result IS NOT NULL
        AND match_result REGEXP '^[0-9]+:[0-9]+$'
        ORDER BY match_time DESC
        LIMIT 20
    ) t;

    SELECT
        (SUM(CASE WHEN ai_result = actual_result THEN 1 ELSE 0 END) / COUNT(*)) * 100
    INTO v_recent_50
    FROM (
        SELECT
            ai_result,
            CASE
                WHEN CAST(SUBSTRING_INDEX(match_result, ':', 1) AS SIGNED) >
                     CAST(SUBSTRING_INDEX(match_result, ':', -1) AS SIGNED) THEN '胜'
                WHEN CAST(SUBSTRING_INDEX(match_result, ':', 1) AS SIGNED) =
                     CAST(SUBSTRING_INDEX(match_result, ':', -1) AS SIGNED) THEN '平'
                WHEN CAST(SUBSTRING_INDEX(match_result, ':', 1) AS SIGNED) <
                     CAST(SUBSTRING_INDEX(match_result, ':', -1) AS SIGNED) THEN '负'
            END AS actual_result
        FROM ai_analysis_result
        WHERE match_result IS NOT NULL
        AND ai_result IS NOT NULL
        AND match_result REGEXP '^[0-9]+:[0-9]+$'
        ORDER BY match_time DESC
        LIMIT 50
    ) t;

    -- 插入统计结果
    INSERT INTO prediction_model_stats (
        id, model_id, model_name, model_type, stats_date, sample_size,
        total_predictions, correct_predictions, accuracy_rate,
        home_win_count, home_win_correct, draw_count, draw_correct,
        away_win_count, away_win_correct,
        recent_10_accuracy, recent_20_accuracy, recent_50_accuracy
    ) VALUES (
        UUID(), v_model_id, '胜负模型v1', 'result', NOW(), 100,
        v_total, v_correct, v_accuracy,
        IFNULL(v_home_win_count, 0), IFNULL(v_home_win_correct, 0),
        IFNULL(v_draw_count, 0), IFNULL(v_draw_correct, 0),
        IFNULL(v_away_win_count, 0), IFNULL(v_away_win_correct, 0),
        IFNULL(v_recent_10, 0), IFNULL(v_recent_20, 0), IFNULL(v_recent_50, 0)
    );

    SELECT '胜负模型v1统计完成' AS result, v_total AS total, v_correct AS correct, v_accuracy AS accuracy_rate;
END$$

DELIMITER ;

-- 创建统计计算的存储过程 - 比分模型
DELIMITER $$

CREATE PROCEDURE `calculate_score_model_accuracy`()
BEGIN
    DECLARE v_model_id VARCHAR(32);
    DECLARE v_total INT;
    DECLARE v_correct INT;
    DECLARE v_accuracy DECIMAL(5,2);
    DECLARE v_recent_10 DECIMAL(5,2);
    DECLARE v_recent_20 DECIMAL(5,2);
    DECLARE v_recent_50 DECIMAL(5,2);

    SET v_model_id = 'MODEL_SCORE_V1';

    -- 计算近100场比分预测准确率
    SELECT
        COUNT(*) INTO v_total
    FROM (
        SELECT * FROM ai_analysis_result
        WHERE match_result IS NOT NULL
        AND ai_score IS NOT NULL
        ORDER BY match_time DESC
        LIMIT 100
    ) t;

    -- 计算正确预测数（比分完全匹配）
    SELECT
        COUNT(*) INTO v_correct
    FROM (
        SELECT * FROM ai_analysis_result
        WHERE match_result IS NOT NULL
        AND ai_score IS NOT NULL
        AND TRIM(ai_score) = TRIM(match_result)
        ORDER BY match_time DESC
        LIMIT 100
    ) t;

    -- 计算准确率
    IF v_total > 0 THEN
        SET v_accuracy = (v_correct / v_total) * 100;
    ELSE
        SET v_accuracy = 0;
    END IF;

    -- 计算近10场准确率
    SELECT
        (SUM(CASE WHEN TRIM(ai_score) = TRIM(match_result) THEN 1 ELSE 0 END) / COUNT(*)) * 100
    INTO v_recent_10
    FROM (
        SELECT ai_score, match_result
        FROM ai_analysis_result
        WHERE match_result IS NOT NULL
        AND ai_score IS NOT NULL
        ORDER BY match_time DESC
        LIMIT 10
    ) t;

    -- 计算近20场准确率
    SELECT
        (SUM(CASE WHEN TRIM(ai_score) = TRIM(match_result) THEN 1 ELSE 0 END) / COUNT(*)) * 100
    INTO v_recent_20
    FROM (
        SELECT ai_score, match_result
        FROM ai_analysis_result
        WHERE match_result IS NOT NULL
        AND ai_score IS NOT NULL
        ORDER BY match_time DESC
        LIMIT 20
    ) t;

    -- 计算近50场准确率
    SELECT
        (SUM(CASE WHEN TRIM(ai_score) = TRIM(match_result) THEN 1 ELSE 0 END) / COUNT(*)) * 100
    INTO v_recent_50
    FROM (
        SELECT ai_score, match_result
        FROM ai_analysis_result
        WHERE match_result IS NOT NULL
        AND ai_score IS NOT NULL
        ORDER BY match_time DESC
        LIMIT 50
    ) t;

    -- 插入统计结果
    INSERT INTO prediction_model_stats (
        id, model_id, model_name, model_type, stats_date, sample_size,
        total_predictions, correct_predictions, accuracy_rate,
        recent_10_accuracy, recent_20_accuracy, recent_50_accuracy
    ) VALUES (
        UUID(), v_model_id, '比分模型v1', 'score', NOW(), 100,
        v_total, v_correct, v_accuracy,
        IFNULL(v_recent_10, 0), IFNULL(v_recent_20, 0), IFNULL(v_recent_50, 0)
    );

    SELECT '比分模型v1统计完成' AS result, v_total AS total, v_correct AS correct, v_accuracy AS accuracy_rate;
END$$

DELIMITER ;

-- 创建查询近100场准确率的视图
CREATE OR REPLACE VIEW `v_latest_model_accuracy` AS
SELECT
    model_name,
    model_type,
    total_predictions,
    correct_predictions,
    accuracy_rate,
    recent_10_accuracy,
    recent_20_accuracy,
    recent_50_accuracy,
    CASE
        WHEN model_type = 'result' THEN
            CONCAT('主胜:', home_win_correct, '/', home_win_count,
                   ' 平局:', draw_correct, '/', draw_count,
                   ' 客胜:', away_win_correct, '/', away_win_count)
        ELSE NULL
    END AS detail_stats,
    stats_date
FROM prediction_model_stats
WHERE (model_name, stats_date) IN (
    SELECT model_name, MAX(stats_date)
    FROM prediction_model_stats
    GROUP BY model_name
)
ORDER BY model_type, model_name;

-- 使用说明
-- 1. 执行以下命令来计算胜负模型准确率：
--    CALL calculate_result_model_accuracy();
--
-- 2. 执行以下命令来计算比分模型准确率：
--    CALL calculate_score_model_accuracy();
--
-- 3. 查看最新的模型准确率统计：
--    SELECT * FROM v_latest_model_accuracy;
--
-- 4. 查看历史统计趋势：
--    SELECT * FROM prediction_model_stats WHERE model_name = '胜负模型v1' ORDER BY stats_date DESC;
--
-- 5. 可以设置定时任务每天自动执行统计：
--    CREATE EVENT IF NOT EXISTS daily_model_stats
--    ON SCHEDULE EVERY 1 DAY
--    STARTS CURRENT_DATE + INTERVAL 1 DAY + INTERVAL 1 HOUR
--    DO BEGIN
--        CALL calculate_result_model_accuracy();
--        CALL calculate_score_model_accuracy();
--    END;
