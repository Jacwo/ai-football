# 单关比赛长龙分析系统使用说明

## 功能概述

统计分析近N场单关比赛（`isSingleMatch=1`）的长龙情况，包括：
- 多久没有出主胜了
- 多久没有出平局了
- 多久没有出客胜了
- 多久没有出最高赔率了
- 多久没有出最低赔率了
- 各类结果的出现次数和出现率
- 历史最长未出记录
- 当前连续未出场次

## 数据来源

系统从以下三张表获取数据：

1. **match_info**：比赛基本信息
   - `isSingleMatch=1`：筛选单关比赛
   - 按 `matchId` 倒序查询（最新的在前）

2. **match_result_detail**：开奖结果
   - `hadResult`：胜平负结果（H=主胜, D=平局, A=客胜）
   - `sectionsNo999`：全场比分

3. **had_list**：赔率信息
   - `h`：主胜赔率
   - `d`：平局赔率
   - `a`：客胜赔率

## API接口

### 分析长龙情况

```
GET /api/dragon-analysis/analyze?sampleSize=30
```

**参数**：
- `sampleSize`（可选）：分析的样本数量，默认30场

**示例**：

```bash
# 分析近30场（默认）
GET http://localhost:8080/api/dragon-analysis/analyze

# 分析近50场
GET http://localhost:8080/api/dragon-analysis/analyze?sampleSize=50

# 分析近100场
GET http://localhost:8080/api/dragon-analysis/analyze?sampleSize=100
```

## 返回数据示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalMatches": 30,
    "gapsSinceLastHomeWin": 2,
    "gapsSinceLastDraw": 0,
    "gapsSinceLastAwayWin": 5,
    "gapsSinceLastMaxOdds": 1,
    "gapsSinceLastMinOdds": 3,
    "maxHomeWinDragon": 8,
    "maxDrawDragon": 12,
    "maxAwayWinDragon": 6,
    "currentHomeWinDragon": 2,
    "currentDrawDragon": 0,
    "currentAwayWinDragon": 5,
    "homeWinCount": 15,
    "drawCount": 8,
    "awayWinCount": 7,
    "homeWinRate": 50.0,
    "drawRate": 26.67,
    "awayWinRate": 23.33,
    "maxOddsWinCount": 5,
    "minOddsWinCount": 18,
    "maxOddsWinRate": 16.67,
    "minOddsWinRate": 60.0,
    "matchDetails": [
      {
        "matchId": 100001,
        "matchNumStr": "周一001",
        "homeTeam": "曼联",
        "awayTeam": "曼城",
        "matchTime": "2025-01-15 20:00:00",
        "fullScore": "2:1",
        "result": "H",
        "resultDesc": "主胜",
        "homeOdds": 1.85,
        "drawOdds": 3.20,
        "awayOdds": 4.50,
        "winningOdds": 1.85,
        "isMaxOdds": false,
        "isMinOdds": true,
        "gapFromNow": 0
      }
      // ... 更多比赛详情
    ]
  }
}
```

## 返回字段说明

### 汇总统计

| 字段 | 说明 |
|------|------|
| totalMatches | 分析的总场次 |
| gapsSinceLastHomeWin | 距离上次主胜多少场（0表示上一场就是主胜） |
| gapsSinceLastDraw | 距离上次平局多少场 |
| gapsSinceLastAwayWin | 距离上次客胜多少场 |
| gapsSinceLastMaxOdds | 距离上次出最高赔率多少场 |
| gapsSinceLastMinOdds | 距离上次出最低赔率多少场 |
| maxHomeWinDragon | 主胜最长未出场次（历史记录） |
| maxDrawDragon | 平局最长未出场次（历史记录） |
| maxAwayWinDragon | 客胜最长未出场次（历史记录） |
| currentHomeWinDragon | 当前主胜连续未出场次 |
| currentDrawDragon | 当前平局连续未出场次 |
| currentAwayWinDragon | 当前客胜连续未出场次 |
| homeWinCount | 主胜出现次数 |
| drawCount | 平局出现次数 |
| awayWinCount | 客胜出现次数 |
| homeWinRate | 主胜出现率(%) |
| drawRate | 平局出现率(%) |
| awayWinRate | 客胜出现率(%) |
| maxOddsWinCount | 最高赔率出现次数 |
| minOddsWinCount | 最低赔率出现次数 |
| maxOddsWinRate | 最高赔率命中率(%) |
| minOddsWinRate | 最低赔率命中率(%) |

### 比赛详情（matchDetails）

| 字段 | 说明 |
|------|------|
| matchId | 比赛ID |
| matchNumStr | 比赛编号 |
| homeTeam | 主队名称 |
| awayTeam | 客队名称 |
| matchTime | 比赛时间 |
| fullScore | 全场比分 |
| result | 开奖结果（H/D/A） |
| resultDesc | 结果描述（主胜/平局/客胜） |
| homeOdds | 主胜赔率 |
| drawOdds | 平局赔率 |
| awayOdds | 客胜赔率 |
| winningOdds | 中奖赔率 |
| isMaxOdds | 是否最高赔率中奖 |
| isMinOdds | 是否最低赔率中奖 |
| gapFromNow | 距离现在第几场（0表示最新一场） |

## 使用场景

### 1. 查看当前长龙情况

```bash
GET /api/dragon-analysis/analyze?sampleSize=30
```

观察：
- 如果 `currentHomeWinDragon` 很大，说明主胜已经连续多场未出
- 如果 `gapsSinceLastDraw` 很大，说明平局很久没有出现了
- 对比 `maxHomeWinDragon` 和 `currentHomeWinDragon`，判断当前长龙是否接近历史记录

### 2. 分析赔率冷门情况

```bash
GET /api/dragon-analysis/analyze?sampleSize=50
```

观察：
- `maxOddsWinRate`：最高赔率的命中率（通常较低）
- `minOddsWinRate`：最低赔率的命中率（通常较高）
- `gapsSinceLastMaxOdds`：判断是否该出冷门了

### 3. 查看历史趋势

通过 `matchDetails` 数组可以看到详细的每场比赛情况，按时间倒序排列（最新的在前）。

## Java代码调用示例

```java
@Autowired
private DragonAnalysisService dragonAnalysisService;

// 分析近30场
DragonAnalysisVO result = dragonAnalysisService.analyzeDragon(30);

System.out.println("总场次: " + result.getTotalMatches());
System.out.println("距离上次主胜: " + result.getGapsSinceLastHomeWin() + " 场");
System.out.println("距离上次平局: " + result.getGapsSinceLastDraw() + " 场");
System.out.println("距离上次客胜: " + result.getGapsSinceLastAwayWin() + " 场");
System.out.println("主胜出现率: " + result.getHomeWinRate() + "%");

// 获取详细比赛列表
List<MatchDragonDetail> details = result.getMatchDetails();
for (MatchDragonDetail detail : details) {
    System.out.println(detail.getMatchNumStr() + " " +
                       detail.getHomeTeam() + " vs " + detail.getAwayTeam() +
                       " -> " + detail.getResultDesc());
}
```

## 实现逻辑说明

### 1. 数据查询

```java
// 查询近N场单关比赛
SELECT * FROM match_info
WHERE is_single_match = 1
ORDER BY match_id DESC
LIMIT 30;

// 查询每场比赛的开奖结果
SELECT * FROM match_result_detail WHERE match_id = ?;

// 查询每场比赛的赔率信息（取最新的一条）
SELECT * FROM had_list
WHERE match_id = ?
ORDER BY update_time DESC
LIMIT 1;
```

### 2. 长龙计算

遍历比赛列表（从最新到最旧）：

- **距离上次出现**：记录第一次出现该结果的位置
- **当前连续未出**：从最新一场开始累计，直到该结果出现为止
- **历史最长未出**：在遍历过程中记录最大的连续未出场次

### 3. 赔率分析

- 比较主胜、平局、客胜三个赔率，确定最高和最低赔率
- 判断中奖的是最高赔率还是最低赔率
- 统计最高/最低赔率的命中次数和命中率

## 注意事项

1. **数据完整性**：
   - 如果某场比赛没有开奖结果或赔率信息，会跳过该场比赛
   - 实际分析的场次可能少于请求的样本数

2. **时间顺序**：
   - 按 `matchId` 倒序排列，最新的比赛在前
   - `gapFromNow=0` 表示最新一场比赛

3. **长龙判断**：
   - 如果整个样本中都没有出现某个结果，距离设为样本总数
   - 例如30场都没有平局，则 `gapsSinceLastDraw=30`

4. **性能考虑**：
   - 建议样本数不超过100场
   - 数据量过大可能影响查询性能

## 技术栈

- **后端框架**：Spring Boot
- **ORM框架**：MyBatis-Plus
- **实体映射**：Lombok
- **日志**：SLF4J

## 文件清单

```
ai-football-model/src/main/java/cn/xingxing/vo/
  ├─ DragonAnalysisVO.java              # 长龙分析结果VO
  └─ MatchDragonDetail.java             # 比赛详情VO

ai-football-starter/src/main/java/cn/xingxing/
  ├─ service/
  │   ├─ DragonAnalysisService.java           # 服务接口
  │   └─ impl/DragonAnalysisServiceImpl.java  # 服务实现（核心分析逻辑）
  └─ web/
      └─ DragonAnalysisController.java        # REST API控制器
```

## 扩展建议

可以根据需要扩展更多分析维度：

1. **按联赛分析**：统计不同联赛的长龙情况
2. **按时间段分析**：分析工作日vs周末的差异
3. **赔率区间分析**：统计不同赔率区间的命中率
4. **组合分析**：分析连续主胜、平局、客胜的模式

## 常见问题

**Q: 为什么返回的场次少于请求的样本数？**
A: 因为某些比赛可能没有开奖结果或赔率信息，这些比赛会被跳过。

**Q: gapsSinceLastHomeWin=0 是什么意思？**
A: 表示最新一场比赛就是主胜。

**Q: 如何判断是否该出冷门了？**
A: 观察 `gapsSinceLastMaxOdds`，如果距离上次出最高赔率已经很多场，结合历史 `maxOddsWinRate`，可以作为参考。

**Q: 长龙记录有什么意义？**
A: 如果 `currentHomeWinDragon` 接近或超过 `maxHomeWinDragon`，说明主胜已经创造或接近历史最长未出记录，可能有补偿性出现的趋势（仅供参考）。
