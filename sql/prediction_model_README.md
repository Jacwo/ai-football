# AI预测模型统计系统使用说明

## 功能概述

统计AI足球预测模型的准确率，支持：
- **胜负模型v1**：统计胜平负预测准确率
- **比分模型v1**：统计比分预测准确率
- **样本数量**：默认近30场，可自定义

## 快速开始

### 1. 初始化数据库

在MySQL中执行SQL脚本：

```sql
SOURCE D:\personal\ai-football\sql\prediction_model.sql;
```

该脚本会创建以下表：
- `prediction_model` - 预测模型表
- `prediction_model_stats` - 模型统计表

并初始化两个模型：胜负模型v1 和 比分模型v1

### 2. 调用API

#### 计算模型准确率

```bash
# 计算近30场准确率（默认）
POST http://localhost:8080/api/prediction-model/calculate/all

# 计算近50场准确率
POST http://localhost:8080/api/prediction-model/calculate/all?sampleSize=50

# 只计算胜负模型
POST http://localhost:8080/api/prediction-model/calculate/result?sampleSize=30

# 只计算比分模型
POST http://localhost:8080/api/prediction-model/calculate/score?sampleSize=30
```

#### 查询统计结果

```bash
# 查询所有模型最新统计（推荐）
GET http://localhost:8080/api/prediction-model/stats/latest

# 查询指定模型的最新统计
GET http://localhost:8080/api/prediction-model/stats/latest/胜负模型v1
GET http://localhost:8080/api/prediction-model/stats/latest/比分模型v1

# 查询模型列表
GET http://localhost:8080/api/prediction-model/list
```

## API接口文档

### 模型管理接口

| 接口 | 方法 | 参数 | 说明 |
|------|------|------|------|
| `/api/prediction-model/list` | GET | - | 查询所有启用的模型 |
| `/api/prediction-model/type/{modelType}` | GET | modelType | 根据类型查询模型（result/score） |

### 统计计算接口

| 接口 | 方法 | 参数 | 说明 |
|------|------|------|------|
| `/api/prediction-model/calculate/all` | POST | sampleSize（可选，默认30） | 计算所有模型准确率 |
| `/api/prediction-model/calculate/result` | POST | sampleSize（可选，默认30） | 计算胜负模型准确率 |
| `/api/prediction-model/calculate/score` | POST | sampleSize（可选，默认30） | 计算比分模型准确率 |

### 统计查询接口

| 接口 | 方法 | 参数 | 说明 |
|------|------|------|------|
| `/api/prediction-model/stats/latest` | GET | - | 查询所有模型最新统计 |
| `/api/prediction-model/stats/latest/{modelName}` | GET | modelName | 查询指定模型最新统计 |
| `/api/prediction-model/stats/type/{modelType}` | GET | modelType | 查询指定类型模型统计 |
| `/api/prediction-model/stats/history/{modelId}` | GET | modelId, startDate, endDate | 查询历史统计数据 |

## 返回数据示例

### 胜负模型统计

```json
{
  "code": 200,
  "data": {
    "modelName": "胜负模型v1",
    "modelType": "result",
    "totalPredictions": 30,
    "correctPredictions": 20,
    "accuracyRate": 66.67,
    "sampleSize": 30,
    "homeWin": "8/12",
    "draw": "5/8",
    "awayWin": "7/10",
    "statsDate": "2025-01-15 10:00:00"
  }
}
```

### 比分模型统计

```json
{
  "code": 200,
  "data": {
    "modelName": "比分模型v1",
    "modelType": "score",
    "totalPredictions": 30,
    "correctPredictions": 8,
    "accuracyRate": 26.67,
    "sampleSize": 30,
    "statsDate": "2025-01-15 10:00:00"
  }
}
```

## 数据说明

### 数据来源

从 `ai_analysis_result` 表获取数据：
- **ai_result**：AI预测的胜平负（"胜"/"平"/"负"）
- **ai_score**：AI预测的比分（如 "2:1"）
- **match_result**：实际比赛结果比分（如 "1:1"）

### 统计逻辑

**胜负模型**：
1. 从 `match_result` 解析实际胜平负结果
2. 与 `ai_result` 对比判断是否预测正确
3. 统计主胜、平局、客胜各自的预测数和正确数

**比分模型**：
1. 直接比较 `ai_score` 和 `match_result` 是否完全匹配
2. 完全匹配才算预测正确

## Java代码调用示例

```java
@Autowired
private PredictionModelService predictionModelService;

// 计算近30场准确率
predictionModelService.calculateAllModelsAccuracy(30);

// 计算近50场准确率
predictionModelService.calculateAllModelsAccuracy(50);

// 查询胜负模型最新统计
PredictionModelStats resultStats =
    predictionModelService.getLatestStatsByName("胜负模型v1");
System.out.println("胜负模型准确率: " + resultStats.getAccuracyRate() + "%");

// 查询比分模型最新统计
PredictionModelStats scoreStats =
    predictionModelService.getLatestStatsByName("比分模型v1");
System.out.println("比分模型准确率: " + scoreStats.getAccuracyRate() + "%");
```

## 数据库查询示例

```sql
-- 查看胜负模型最新统计
SELECT * FROM prediction_model_stats
WHERE model_name = '胜负模型v1'
ORDER BY stats_date DESC
LIMIT 1;

-- 查看比分模型最新统计
SELECT * FROM prediction_model_stats
WHERE model_name = '比分模型v1'
ORDER BY stats_date DESC
LIMIT 1;

-- 查看所有统计历史
SELECT
    model_name,
    sample_size,
    accuracy_rate,
    total_predictions,
    correct_predictions,
    stats_date
FROM prediction_model_stats
ORDER BY stats_date DESC;
```

## 注意事项

1. **数据质量**：
   - `match_result` 必须是 "数字:数字" 格式
   - `ai_result` 必须是 "胜"、"平" 或 "负"
   - 数据格式不符的记录会被跳过

2. **样本数量**：
   - 默认统计近30场
   - 可通过API参数自定义（如50场、100场）
   - 如果实际数据少于指定样本数，会统计所有可用数据

3. **统计时机**：
   - 建议在比赛结果更新后执行统计
   - 可以多次执行，每次会生成新的统计记录

4. **性能**：
   - 统计逻辑在Java代码中执行，无需存储过程
   - 适合中小规模数据量（每次统计几十到几百场比赛）

## 技术栈

- **数据库**：MySQL 5.7+
- **ORM框架**：MyBatis-Plus
- **后端框架**：Spring Boot
- **实体映射**：Lombok
- **日志**：SLF4J

## 文件清单

```
sql/
  └─ prediction_model.sql               # 数据库表结构

ai-football-model/src/main/java/cn/xingxing/entity/
  ├─ PredictionModel.java              # 预测模型实体类
  └─ PredictionModelStats.java         # 模型统计实体类

ai-football-repository/src/main/java/cn/xingxing/mapper/
  ├─ PredictionModelMapper.java        # 模型Mapper
  ├─ PredictionModelStatsMapper.java   # 统计Mapper
  └─ AiAnalysisResultMapper.java       # AI分析结果Mapper（已扩展）

ai-football-starter/src/main/java/cn/xingxing/
  ├─ service/
  │   ├─ PredictionModelService.java          # 服务接口
  │   └─ impl/PredictionModelServiceImpl.java # 服务实现（统计逻辑）
  └─ web/
      └─ PredictionModelController.java       # REST API控制器
```

## 常见问题

**Q: 为什么准确率显示0？**
A: 检查 `ai_analysis_result` 表是否有 `match_result` 不为空的数据。

**Q: 如何查看详细的预测情况？**
A: 胜负模型的统计结果包含 `homeWin`, `draw`, `awayWin` 字段，显示各类预测的正确数/总数。

**Q: 可以统计更多场次吗？**
A: 可以，通过API的 `sampleSize` 参数指定，如 `?sampleSize=100`。

**Q: 统计数据会覆盖吗？**
A: 不会，每次执行统计会生成新的记录，可以查看历史趋势。
