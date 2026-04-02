# AI预测模型统计系统使用说明

## 概述

本系统用于统计和跟踪AI足球预测模型的准确率，包括：
- **胜负模型v1**：统计近100场比赛的胜平负预测准确率
- **比分模型v1**：统计近100场比赛的比分预测准确率

## 数据库表结构

### 1. prediction_model（预测模型表）
存储AI预测模型的基本信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(32) | 主键ID |
| model_name | VARCHAR(100) | 模型名称 |
| model_type | VARCHAR(20) | 模型类型 (result:胜负预测, score:比分预测) |
| model_version | VARCHAR(20) | 模型版本 |
| description | VARCHAR(500) | 模型描述 |
| is_active | TINYINT(1) | 是否启用 (1:启用, 0:禁用) |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 2. prediction_model_stats（预测模型准确率统计表）
存储模型的统计数据和准确率信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(32) | 主键ID |
| model_id | VARCHAR(32) | 模型ID |
| model_name | VARCHAR(100) | 模型名称 |
| model_type | VARCHAR(20) | 模型类型 |
| stats_date | DATETIME | 统计时间 |
| sample_size | INT | 样本数量 (默认近100场) |
| total_predictions | INT | 总预测场次 |
| correct_predictions | INT | 正确预测数 |
| accuracy_rate | DECIMAL(5,2) | 准确率 (%) |
| home_win_count | INT | 主胜预测数 |
| home_win_correct | INT | 主胜预测正确数 |
| draw_count | INT | 平局预测数 |
| draw_correct | INT | 平局预测正确数 |
| away_win_count | INT | 客胜预测数 |
| away_win_correct | INT | 客胜预测正确数 |
| recent_10_accuracy | DECIMAL(5,2) | 近10场准确率 |
| recent_20_accuracy | DECIMAL(5,2) | 近20场准确率 |
| recent_50_accuracy | DECIMAL(5,2) | 近50场准确率 |
| league_stats | TEXT | 分联赛统计 JSON格式 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

## 初始化步骤

### 1. 执行SQL脚本
```sql
-- 在数据库中执行以下脚本
SOURCE D:\personal\ai-football\sql\prediction_model.sql;
```

该脚本会：
- 创建 `prediction_model` 表
- 创建 `prediction_model_stats` 表
- 初始化两个模型数据（胜负模型v1 和 比分模型v1）
- 创建两个存储过程用于计算准确率
- 创建一个视图 `v_latest_model_accuracy` 用于查询最新统计

### 2. 数据源说明

系统从 `ai_analysis_result` 表获取数据：
- `ai_result`：AI预测的胜平负结果（如："胜"、"平"、"负"）
- `ai_score`：AI预测的比分（如："2:1"）
- `match_result`：实际比赛结果比分（如："1:1"）

**注意**：
- 胜负判断逻辑：通过解析 `match_result` 比分来确定实际胜平负结果
- 比分判断逻辑：直接比较 `ai_score` 和 `match_result` 是否完全匹配

## 使用方法

### 方法一：直接调用存储过程（SQL）

```sql
-- 1. 计算胜负模型准确率
CALL calculate_result_model_accuracy();

-- 2. 计算比分模型准确率
CALL calculate_score_model_accuracy();

-- 3. 查看最新的模型准确率统计
SELECT * FROM v_latest_model_accuracy;

-- 4. 查看胜负模型的历史统计趋势
SELECT * FROM prediction_model_stats
WHERE model_name = '胜负模型v1'
ORDER BY stats_date DESC;

-- 5. 查看比分模型的历史统计趋势
SELECT * FROM prediction_model_stats
WHERE model_name = '比分模型v1'
ORDER BY stats_date DESC;
```

### 方法二：通过REST API调用

#### 1. 执行模型准确率计算

**计算胜负模型准确率**
```
POST /api/prediction-model/calculate/result
响应: { "code": 200, "message": "胜负模型准确率计算成功" }
```

**计算比分模型准确率**
```
POST /api/prediction-model/calculate/score
响应: { "code": 200, "message": "比分模型准确率计算成功" }
```

**计算所有模型准确率**
```
POST /api/prediction-model/calculate/all
响应: { "code": 200, "message": "所有模型准确率计算成功" }
```

#### 2. 查询模型统计数据

**查询所有模型的最新统计**
```
GET /api/prediction-model/stats/latest
响应: 返回所有模型的最新统计数据列表
```

**查询模型统计汇总（推荐）**
```
GET /api/prediction-model/stats/summary
响应: {
  "code": 200,
  "data": {
    "resultModel": {
      "modelName": "胜负模型v1",
      "totalPredictions": 100,
      "correctPredictions": 65,
      "accuracyRate": 65.00,
      "recent10Accuracy": 70.00,
      "recent20Accuracy": 68.00,
      "recent50Accuracy": 66.00,
      "homeWin": "30/45",
      "draw": "15/25",
      "awayWin": "20/30",
      "statsDate": "2025-01-15 10:00:00"
    },
    "scoreModel": {
      "modelName": "比分模型v1",
      "totalPredictions": 100,
      "correctPredictions": 25,
      "accuracyRate": 25.00,
      "recent10Accuracy": 30.00,
      "recent20Accuracy": 28.00,
      "recent50Accuracy": 26.00,
      "statsDate": "2025-01-15 10:00:00"
    }
  }
}
```

**查询指定模型的最新统计**
```
GET /api/prediction-model/stats/latest/{modelName}
例如: GET /api/prediction-model/stats/latest/胜负模型v1
```

**查询指定类型的模型统计**
```
GET /api/prediction-model/stats/type/{modelType}
例如: GET /api/prediction-model/stats/type/result  # 查询所有胜负预测模型
     GET /api/prediction-model/stats/type/score   # 查询所有比分预测模型
```

**查询模型历史统计数据**
```
GET /api/prediction-model/stats/history/{modelId}?startDate=2025-01-01 00:00:00&endDate=2025-01-31 23:59:59
```

#### 3. 查询模型信息

**查询所有启用的模型**
```
GET /api/prediction-model/list
```

**根据模型类型查询模型**
```
GET /api/prediction-model/type/{modelType}
例如: GET /api/prediction-model/type/result
```

### 方法三：通过Java代码调用

```java
@Autowired
private PredictionModelService predictionModelService;

// 1. 计算所有模型准确率
predictionModelService.calculateAllModelsAccuracy();

// 2. 仅计算胜负模型准确率
predictionModelService.calculateResultModelAccuracy();

// 3. 仅计算比分模型准确率
predictionModelService.calculateScoreModelAccuracy();

// 4. 查询胜负模型的最新统计
PredictionModelStats resultStats = predictionModelService.getLatestStatsByName("胜负模型v1");
System.out.println("胜负模型准确率: " + resultStats.getAccuracyRate() + "%");

// 5. 查询比分模型的最新统计
PredictionModelStats scoreStats = predictionModelService.getLatestStatsByName("比分模型v1");
System.out.println("比分模型准确率: " + scoreStats.getAccuracyRate() + "%");

// 6. 查询所有模型的最新统计
List<Map<String, Object>> allStats = predictionModelService.getAllLatestStats();
```

## 自动化统计

### 设置定时任务（可选）

如果需要每天自动执行统计，可以创建MySQL事件：

```sql
-- 创建每天自动执行的事件
CREATE EVENT IF NOT EXISTS daily_model_stats
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_DATE + INTERVAL 1 DAY + INTERVAL 1 HOUR  -- 每天凌晨1点执行
DO BEGIN
    CALL calculate_result_model_accuracy();
    CALL calculate_score_model_accuracy();
END;

-- 启用事件调度器（如果未启用）
SET GLOBAL event_scheduler = ON;

-- 查看事件状态
SHOW EVENTS;

-- 删除事件（如果需要）
DROP EVENT IF EXISTS daily_model_stats;
```

### 或使用Spring定时任务

在Java代码中添加定时任务：

```java
@Component
public class ModelStatsScheduler {

    @Autowired
    private PredictionModelService predictionModelService;

    // 每天凌晨1点执行
    @Scheduled(cron = "0 0 1 * * ?")
    public void calculateDailyStats() {
        log.info("开始每日模型统计...");
        predictionModelService.calculateAllModelsAccuracy();
        log.info("每日模型统计完成");
    }
}
```

## 统计指标说明

### 胜负模型统计指标

| 指标 | 说明 |
|------|------|
| 准确率 | 近100场比赛中胜平负预测正确的比例 |
| 主胜预测 | 预测主队获胜的场次和正确数 |
| 平局预测 | 预测平局的场次和正确数 |
| 客胜预测 | 预测客队获胜的场次和正确数 |
| 近10场准确率 | 最近10场比赛的准确率 |
| 近20场准确率 | 最近20场比赛的准确率 |
| 近50场准确率 | 最近50场比赛的准确率 |

### 比分模型统计指标

| 指标 | 说明 |
|------|------|
| 准确率 | 近100场比赛中比分预测完全正确的比例 |
| 近10场准确率 | 最近10场比赛的准确率 |
| 近20场准确率 | 最近20场比赛的准确率 |
| 近50场准确率 | 最近50场比赛的准确率 |

## 注意事项

1. **数据依赖**：统计依赖于 `ai_analysis_result` 表中的数据，确保该表有足够的历史数据
2. **数据格式**：
   - `match_result` 必须是 "数字:数字" 格式，如 "2:1"
   - `ai_result` 必须是 "胜"、"平" 或 "负"
   - `ai_score` 必须是 "数字:数字" 格式
3. **性能考虑**：如果数据量很大，建议在非业务高峰期执行统计计算
4. **统计时机**：建议在比赛结果更新后，再执行统计计算，以获得最新的准确率数据

## 扩展说明

如果需要添加新的模型版本，可以：

```sql
-- 1. 添加新模型
INSERT INTO prediction_model (id, model_name, model_type, model_version, description, is_active)
VALUES ('MODEL_RESULT_V2', '胜负模型v2', 'result', 'v2', '改进版的胜负预测模型', 1);

-- 2. 创建对应的统计存储过程（参考现有存储过程）
-- 3. 在Service和Controller中添加相应的调用方法
```

## 技术栈

- **数据库**：MySQL 5.7+
- **ORM框架**：MyBatis-Plus
- **后端框架**：Spring Boot
- **实体映射**：Lombok
- **日志**：SLF4J

## 文件清单

- `sql/prediction_model.sql` - 数据库表结构和存储过程
- `entity/PredictionModel.java` - 预测模型实体类
- `entity/PredictionModelStats.java` - 模型统计实体类
- `mapper/PredictionModelMapper.java` - 模型Mapper接口
- `mapper/PredictionModelStatsMapper.java` - 统计Mapper接口
- `service/PredictionModelService.java` - 服务接口
- `service/impl/PredictionModelServiceImpl.java` - 服务实现类
- `web/PredictionModelController.java` - REST API控制器

## 示例数据查询

```sql
-- 查看近期表现最好的模型
SELECT
    model_name,
    model_type,
    recent_10_accuracy,
    recent_20_accuracy,
    accuracy_rate,
    stats_date
FROM v_latest_model_accuracy
ORDER BY recent_10_accuracy DESC;

-- 对比不同时期的准确率
SELECT
    model_name,
    DATE(stats_date) as date,
    accuracy_rate,
    recent_10_accuracy
FROM prediction_model_stats
WHERE model_name = '胜负模型v1'
ORDER BY stats_date DESC
LIMIT 30;
```
