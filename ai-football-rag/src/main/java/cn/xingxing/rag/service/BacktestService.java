package cn.xingxing.rag.service;

import cn.xingxing.entity.AiAnalysisResult;
import cn.xingxing.rag.entity.MatchKnowledge;
import cn.xingxing.rag.entity.PredictionStats;
import cn.xingxing.rag.mapper.MatchKnowledgeMapper;
import cn.xingxing.rag.mapper.PredictionStatsMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 预测回测服务
 * 评估AI预测的历史准确率，生成统计报告
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BacktestService {

    private final KnowledgeVectorStore vectorStore;
    private final MatchKnowledgeMapper matchKnowledgeMapper;
    private final PredictionStatsMapper predictionStatsMapper;

    /**
     * 从历史分析结果构建知识库
     * 将AiAnalysisResult转换为MatchKnowledge并加入向量存储和数据库
     */
    public int buildKnowledgeFromHistory(List<AiAnalysisResult> results) {
        int count = 0;
        for (AiAnalysisResult result : results) {
            if (result.getMatchResult() != null && !result.getMatchResult().isEmpty()) {
                MatchKnowledge knowledge = convertToKnowledge(result);

                // 检查是否已存在
                LambdaQueryWrapper<MatchKnowledge> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(MatchKnowledge::getMatchId, knowledge.getMatchId());
                MatchKnowledge existing = matchKnowledgeMapper.selectOne(queryWrapper);

                if (existing == null) {
                    // 保存到数据库
                    matchKnowledgeMapper.insert(knowledge);
                } else {
                    // 更新已存在的记录
                    knowledge.setId(existing.getId());
                    knowledge.setUpdateTime(LocalDateTime.now());
                    matchKnowledgeMapper.updateById(knowledge);
                }

                // 添加到向量存储
                vectorStore.addKnowledge(knowledge);
                count++;
            }
        }
        log.info("从历史分析结果构建了 {} 条知识（已同步到数据库）", count);
        return count;
    }

    /**
     * 从数据库加载知识到向量存储
     */
    public int loadKnowledgeFromDatabase() {
        List<MatchKnowledge> knowledgeList = matchKnowledgeMapper.selectList(null);
        if (knowledgeList.isEmpty()) {
            log.info("数据库中暂无知识记录");
            return 0;
        }

        vectorStore.clear();
        vectorStore.addKnowledgeBatch(knowledgeList);
        log.info("从数据库加载了 {} 条知识到向量存储", knowledgeList.size());
        return knowledgeList.size();
    }

    /**
     * 保存单条知识到数据库
     */
    public void saveKnowledge(MatchKnowledge knowledge) {
        LambdaQueryWrapper<MatchKnowledge> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MatchKnowledge::getMatchId, knowledge.getMatchId());
        MatchKnowledge existing = matchKnowledgeMapper.selectOne(queryWrapper);

        if (existing == null) {
            matchKnowledgeMapper.insert(knowledge);
        } else {
            knowledge.setId(existing.getId());
            knowledge.setUpdateTime(LocalDateTime.now());
            matchKnowledgeMapper.updateById(knowledge);
        }

        vectorStore.addKnowledge(knowledge);
        log.debug("知识已保存: {} vs {}", knowledge.getHomeTeam(), knowledge.getAwayTeam());
    }

    /**
     * 保存每日统计到数据库
     */
    public void saveDailyStats(List<PredictionStats> statsList) {
        for (PredictionStats stats : statsList) {
            LambdaQueryWrapper<PredictionStats> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PredictionStats::getStatsDate, stats.getStatsDate());
            if (stats.getLeague() != null) {
                queryWrapper.eq(PredictionStats::getLeague, stats.getLeague());
            } else {
                queryWrapper.isNull(PredictionStats::getLeague);
            }

            PredictionStats existing = predictionStatsMapper.selectOne(queryWrapper);
            if (existing == null) {
                predictionStatsMapper.insert(stats);
            } else {
                stats.setId(existing.getId());
                stats.setUpdateTime(LocalDateTime.now());
                predictionStatsMapper.updateById(stats);
            }
        }
        log.info("已保存 {} 条每日统计数据", statsList.size());
    }

    /**
     * 计算整体预测准确率
     */
    public BacktestReport generateBacktestReport(List<AiAnalysisResult> results) {
        BacktestReport report = new BacktestReport();
        report.setGenerateTime(LocalDateTime.now());

        // 过滤有结果的比赛
        List<AiAnalysisResult> completedMatches = results.stream()
            .filter(r -> r.getMatchResult() != null && !r.getMatchResult().isEmpty())
            .filter(r -> r.getAiResult() != null && !r.getAiResult().isEmpty())
            .toList();

        if (completedMatches.isEmpty()) {
            report.setTotalMatches(0);
            return report;
        }

        report.setTotalMatches(completedMatches.size());

        // 胜负预测准确率
        long correctResults = completedMatches.stream()
            .filter(this::isResultCorrect)
            .count();
        report.setCorrectResultPredictions((int) correctResults);
        report.setResultAccuracy(calculatePercentage(correctResults, completedMatches.size()));

        // 比分预测准确率
        long correctScores = completedMatches.stream()
            .filter(this::isScoreCorrect)
            .count();
        report.setCorrectScorePredictions((int) correctScores);
        report.setScoreAccuracy(calculatePercentage(correctScores, completedMatches.size()));

        // 按联赛分组统计
        Map<String, LeagueStats> leagueStatsMap = calculateLeagueStats(completedMatches);
        report.setLeagueStats(leagueStatsMap);

        // 冷门预测统计
        report.setUpsetStats(calculateUpsetStats(completedMatches));

        // 计算ROI
        report.setRoi(calculateROI(completedMatches));

        // 生成改进建议
        report.setImprovementSuggestions(generateImprovementSuggestions(report));

        log.info("回测报告生成完成: 总场次={}, 胜负准确率={}, 比分准确率={}",
            report.getTotalMatches(), report.getResultAccuracy(), report.getScoreAccuracy());

        return report;
    }

    /**
     * 按时间段生成趋势分析
     */
    public List<PredictionStats> generateDailyStats(List<AiAnalysisResult> results) {
        // 按日期分组
        Map<LocalDate, List<AiAnalysisResult>> dailyResults = results.stream()
            .filter(r -> r.getMatchTime() != null)
            .filter(r -> r.getMatchResult() != null && !r.getMatchResult().isEmpty())
            .collect(Collectors.groupingBy(r -> r.getMatchTime().toLocalDate()));

        List<PredictionStats> statsList = new ArrayList<>();

        for (Map.Entry<LocalDate, List<AiAnalysisResult>> entry : dailyResults.entrySet()) {
            PredictionStats stats = new PredictionStats();
            stats.setStatsDate(entry.getKey());

            List<AiAnalysisResult> dayResults = entry.getValue();
            stats.setTotalPredictions(dayResults.size());

            long correct = dayResults.stream().filter(this::isResultCorrect).count();
            stats.setCorrectPredictions((int) correct);
            stats.setResultAccuracy(calculatePercentage(correct, dayResults.size()));

            long scoreCorrect = dayResults.stream().filter(this::isScoreCorrect).count();
            stats.setCorrectScores((int) scoreCorrect);
            stats.setScoreAccuracy(calculatePercentage(scoreCorrect, dayResults.size()));

            stats.setCreateTime(LocalDateTime.now());
            statsList.add(stats);
        }

        // 按日期排序
        statsList.sort(Comparator.comparing(PredictionStats::getStatsDate));
        return statsList;
    }

    /**
     * 分析预测错误的模式
     */
    public ErrorPatternAnalysis analyzeErrorPatterns(List<AiAnalysisResult> results) {
        ErrorPatternAnalysis analysis = new ErrorPatternAnalysis();

        List<AiAnalysisResult> wrongPredictions = results.stream()
            .filter(r -> r.getMatchResult() != null && !r.getMatchResult().isEmpty())
            .filter(r -> !isResultCorrect(r))
            .toList();

        if (wrongPredictions.isEmpty()) {
            return analysis;
        }

        // 分析错误类型分布
        Map<String, Integer> errorTypes = new HashMap<>();
        for (AiAnalysisResult result : wrongPredictions) {
            String errorType = classifyError(result);
            errorTypes.merge(errorType, 1, Integer::sum);
        }
        analysis.setErrorTypeDistribution(errorTypes);

        // 分析哪种预测最容易出错
        Map<String, Integer> predictionErrors = wrongPredictions.stream()
            .collect(Collectors.groupingBy(
                r -> r.getAiResult() != null ? r.getAiResult() : "未知",
                Collectors.summingInt(r -> 1)
            ));
        analysis.setPredictionTypeErrors(predictionErrors);

        // 分析赔率区间的错误分布
        analysis.setOddsRangeErrors(analyzeOddsRangeErrors(wrongPredictions));

        return analysis;
    }

    private MatchKnowledge convertToKnowledge(AiAnalysisResult result) {
        MatchKnowledge knowledge = new MatchKnowledge();
        knowledge.setMatchId(result.getMatchId());
        knowledge.setHomeTeam(result.getHomeTeam());
        knowledge.setAwayTeam(result.getAwayTeam());
        knowledge.setMatchTime(result.getMatchTime());

        // aiResult 是预测比分（如 "2:1"），转换为胜负结果存储
        String predictedScore = result.getAiResult();
        knowledge.setAiScore(predictedScore);
        knowledge.setAiPrediction(scoreToOutcome(predictedScore)); // 从比分推断胜负

        // matchResult 是实际胜负（如 "主胜"）
        knowledge.setActualResult(result.getMatchResult());
        // aiScore 可能存储实际比分
        knowledge.setActualScore(result.getAiScore());

        // 评估预测是否正确
        knowledge.setPredictionCorrect(isResultCorrect(result));
        knowledge.setScoreCorrect(isScoreCorrect(result));

        // 构建赔率快照
        if (result.getHomeWin() != null) {
            knowledge.setOddsSnapshot(String.format("{\"h\": %s, \"d\": %s, \"a\": %s}",
                result.getHomeWin(), result.getDraw(), result.getAwayWin()));
        }

        // 从AI分析中提取摘要
        if (result.getAiAnalysis() != null) {
            knowledge.setAnalysisSummary(extractSummary(result.getAiAnalysis()));
        }

        // 从赛后复盘中提取学习经验
        if (result.getAfterMatchAnalysis() != null) {
            knowledge.setLearningInsight(extractLearningInsight(result.getAfterMatchAnalysis()));
        }

        // 根据赔率判断场景标签
        knowledge.setScenarioTags(inferScenarioTags(result));

        knowledge.setCreateTime(LocalDateTime.now());
        return knowledge;
    }

    private boolean isResultCorrect(AiAnalysisResult result) {
        if (result.getAiResult() == null || result.getMatchResult() == null) {
            return false;
        }

        // aiResult 是比分（如 "0:1", "2:1"），需要转换为胜负
        String predictedOutcome = scoreToOutcome(result.getAiResult().trim());
        // matchResult 是胜负（如 "主胜", "平局", "客胜"）
        String actualOutcome = normalizeResult(result.getMatchResult().trim());

        return predictedOutcome.equals(actualOutcome);
    }

    /**
     * 将比分转换为胜负结果
     * @param score 比分，如 "2:1", "0:0", "1:3"
     * @return 胜负结果：主胜/平局/客胜
     */
    private String scoreToOutcome(String score) {
        if (score == null || score.isEmpty()) {
            return "";
        }

        // 尝试解析比分
        String[] parts = score.split("[:：-]");
        if (parts.length != 2) {
            // 如果不是比分格式，可能已经是胜负结果，直接标准化
            return normalizeResult(score);
        }

        try {
            int homeGoals = Integer.parseInt(parts[0].trim());
            int awayGoals = Integer.parseInt(parts[1].trim());

            if (homeGoals > awayGoals) {
                return "主胜";
            } else if (homeGoals < awayGoals) {
                return "客胜";
            } else {
                return "平局";
            }
        } catch (NumberFormatException e) {
            // 解析失败，尝试标准化
            return normalizeResult(score);
        }
    }

    private String normalizeResult(String result) {
        if (result == null) return "";
        result = result.trim().toLowerCase();

        if (result.contains("主胜") || result.contains("主队") || result.equals("3")) {
            return "主胜";
        } else if (result.contains("客胜") || result.contains("客队") || result.equals("0")) {
            return "客胜";
        } else if (result.contains("平") || result.equals("1")) {
            return "平局";
        }
        return result;
    }

    private boolean isScoreCorrect(AiAnalysisResult result) {
        // aiResult 是预测比分，aiScore 也可能存比分
        String predictedScore = result.getAiResult();
        String actualScore = result.getAiScore(); // 实际比分可能存在 aiScore 字段

        if (predictedScore == null || actualScore == null) {
            return false;
        }

        // 标准化比分格式后比较
        return normalizeScore(predictedScore).equals(normalizeScore(actualScore));
    }

    /**
     * 标准化比分格式
     * 将 "2:1", "2-1", "2：1" 等统一为 "2:1" 格式
     */
    private String normalizeScore(String score) {
        if (score == null) return "";
        // 移除空格，统一分隔符为冒号
        return score.trim()
            .replace(" ", "")
            .replace("-", ":")
            .replace("：", ":");
    }

    private BigDecimal calculatePercentage(long correct, int total) {
        if (total == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(correct * 100.0 / total)
            .setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, LeagueStats> calculateLeagueStats(List<AiAnalysisResult> results) {
        // 假设可以从某处获取联赛信息，这里简化处理
        return new HashMap<>();
    }

    private UpsetStats calculateUpsetStats(List<AiAnalysisResult> results) {
        UpsetStats stats = new UpsetStats();

        // 识别冷门预测（预测了高赔率结果）
        List<AiAnalysisResult> upsetPredictions = results.stream()
            .filter(this::isUpsetPrediction)
            .toList();

        stats.setTotalUpsetPredictions(upsetPredictions.size());

        long correctUpsets = upsetPredictions.stream()
            .filter(this::isResultCorrect)
            .count();
        stats.setCorrectUpsetPredictions((int) correctUpsets);

        if (!upsetPredictions.isEmpty()) {
            stats.setUpsetAccuracy(calculatePercentage(correctUpsets, upsetPredictions.size()));
        }

        return stats;
    }

    private boolean isUpsetPrediction(AiAnalysisResult result) {
        // 如果预测的赔率 > 3.0，认为是冷门预测
        try {
            String prediction = result.getAiResult();
            String odds = null;

            if (prediction != null) {
                if (prediction.contains("主胜")) {
                    odds = result.getHomeWin();
                } else if (prediction.contains("客胜")) {
                    odds = result.getAwayWin();
                } else if (prediction.contains("平")) {
                    odds = result.getDraw();
                }
            }

            if (odds != null) {
                return Double.parseDouble(odds) >= 3.0;
            }
        } catch (Exception e) {
            // ignore
        }
        return false;
    }

    private BigDecimal calculateROI(List<AiAnalysisResult> results) {
        // 简化的ROI计算：假设每场投注1单位
        double totalInvest = results.size();
        double totalReturn = 0;

        for (AiAnalysisResult result : results) {
            if (isResultCorrect(result)) {
                // 返还 = 赔率
                try {
                    String odds = getOddsForPrediction(result);
                    if (odds != null) {
                        totalReturn += Double.parseDouble(odds);
                    }
                } catch (Exception e) {
                    totalReturn += 1.5; // 默认赔率
                }
            }
        }

        if (totalInvest == 0) return BigDecimal.ZERO;

        double roi = ((totalReturn - totalInvest) / totalInvest) * 100;
        return BigDecimal.valueOf(roi).setScale(2, RoundingMode.HALF_UP);
    }

    private String getOddsForPrediction(AiAnalysisResult result) {
        String prediction = result.getAiResult();
        if (prediction == null) return null;

        if (prediction.contains("主胜")) {
            return result.getHomeWin();
        } else if (prediction.contains("客胜")) {
            return result.getAwayWin();
        } else if (prediction.contains("平")) {
            return result.getDraw();
        }
        return null;
    }

    private List<String> generateImprovementSuggestions(BacktestReport report) {
        List<String> suggestions = new ArrayList<>();

        if (report.getResultAccuracy().compareTo(BigDecimal.valueOf(50)) < 0) {
            suggestions.add("整体预测准确率低于50%，建议重新审视分析模型和特征权重");
        }

        if (report.getUpsetStats() != null &&
            report.getUpsetStats().getUpsetAccuracy() != null &&
            report.getUpsetStats().getUpsetAccuracy().compareTo(BigDecimal.valueOf(30)) < 0) {
            suggestions.add("冷门预测准确率较低，建议减少冷门预测的激进程度");
        }

        if (report.getRoi() != null && report.getRoi().compareTo(BigDecimal.ZERO) < 0) {
            suggestions.add("ROI为负，建议提高预测准确率或调整投注策略");
        }

        return suggestions;
    }

    private String extractSummary(String aiAnalysis) {
        if (aiAnalysis == null || aiAnalysis.isEmpty()) {
            return "";
        }
        // 提取前200字作为摘要
        return aiAnalysis.length() > 200 ? aiAnalysis.substring(0, 200) : aiAnalysis;
    }

    private String extractLearningInsight(String afterMatchAnalysis) {
        if (afterMatchAnalysis == null || afterMatchAnalysis.isEmpty()) {
            return "";
        }
        // 提取关键经验
        return afterMatchAnalysis.length() > 300 ? afterMatchAnalysis.substring(0, 300) : afterMatchAnalysis;
    }

    private String inferScenarioTags(AiAnalysisResult result) {
        List<String> tags = new ArrayList<>();

        try {
            double h = Double.parseDouble(result.getHomeWin());
            double a = Double.parseDouble(result.getAwayWin());

            // 强弱对决
            if (h < 1.5) {
                tags.add("强队主场");
            } else if (a < 1.5) {
                tags.add("强队客场");
            }

            // 平局倾向
            double d = Double.parseDouble(result.getDraw());
            if (d < 3.2) {
                tags.add("平局概率高");
            }

            // 冷门场景
            if (Math.min(h, a) > 2.5) {
                tags.add("势均力敌");
            }
        } catch (Exception e) {
            // ignore
        }

        return String.join(",", tags);
    }

    private String classifyError(AiAnalysisResult result) {
        String prediction = normalizeResult(result.getAiResult());
        String actual = normalizeResult(result.getMatchResult());

        return String.format("预测%s->实际%s", prediction, actual);
    }

    private Map<String, Integer> analyzeOddsRangeErrors(List<AiAnalysisResult> wrongPredictions) {
        Map<String, Integer> rangeErrors = new HashMap<>();

        for (AiAnalysisResult result : wrongPredictions) {
            try {
                String odds = getOddsForPrediction(result);
                if (odds != null) {
                    double oddsValue = Double.parseDouble(odds);
                    String range;
                    if (oddsValue < 1.5) {
                        range = "低赔(<1.5)";
                    } else if (oddsValue < 2.0) {
                        range = "中低赔(1.5-2.0)";
                    } else if (oddsValue < 3.0) {
                        range = "中赔(2.0-3.0)";
                    } else {
                        range = "高赔(>3.0)";
                    }
                    rangeErrors.merge(range, 1, Integer::sum);
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return rangeErrors;
    }

    // 内部类定义

    @lombok.Data
    public static class BacktestReport {
        private LocalDateTime generateTime;
        private int totalMatches;
        private int correctResultPredictions;
        private int correctScorePredictions;
        private BigDecimal resultAccuracy;
        private BigDecimal scoreAccuracy;
        private BigDecimal roi;
        private Map<String, LeagueStats> leagueStats;
        private UpsetStats upsetStats;
        private List<String> improvementSuggestions;
    }

    @lombok.Data
    public static class LeagueStats {
        private String league;
        private int total;
        private int correct;
        private BigDecimal accuracy;
    }

    @lombok.Data
    public static class UpsetStats {
        private int totalUpsetPredictions;
        private int correctUpsetPredictions;
        private BigDecimal upsetAccuracy;
    }

    @lombok.Data
    public static class ErrorPatternAnalysis {
        private Map<String, Integer> errorTypeDistribution;
        private Map<String, Integer> predictionTypeErrors;
        private Map<String, Integer> oddsRangeErrors;
    }
}
