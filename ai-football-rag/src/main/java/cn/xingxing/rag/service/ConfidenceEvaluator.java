package cn.xingxing.rag.service;

import cn.xingxing.dto.MatchAnalysis;
import cn.xingxing.rag.entity.MatchKnowledge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 预测置信度评估服务
 * 基于多维度数据评估AI预测的可信度
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfidenceEvaluator {

    private final KnowledgeVectorStore vectorStore;

    /**
     * 评估预测置信度
     *
     * @param analysis 比赛分析数据
     * @param aiPrediction AI预测结果 (主胜/平局/客胜)
     * @return 置信度分数 (0-100)
     */
    public ConfidenceResult evaluate(MatchAnalysis analysis, String aiPrediction) {
        ConfidenceResult result = new ConfidenceResult();
        int totalScore = 0;
        int factors = 0;

        // 1. 数据完整度评分 (0-20分)
        int dataScore = evaluateDataCompleteness(analysis);
        totalScore += dataScore;
        factors++;
        result.setDataCompletenessScore(dataScore);

        // 2. 赔率一致性评分 (0-25分)
        int oddsScore = evaluateOddsConsistency(analysis, aiPrediction);
        totalScore += oddsScore;
        factors++;
        result.setOddsConsistencyScore(oddsScore);

        // 3. 历史验证评分 (0-25分)
        int historyScore = evaluateHistoricalAccuracy(analysis);
        totalScore += historyScore;
        factors++;
        result.setHistoricalScore(historyScore);

        // 4. xG数据支撑评分 (0-15分)
        int xgScore = evaluateXGSupport(analysis, aiPrediction);
        totalScore += xgScore;
        factors++;
        result.setXgSupportScore(xgScore);

        // 5. 相似案例准确率评分 (0-15分)
        int similarScore = evaluateSimilarCasesAccuracy(analysis);
        totalScore += similarScore;
        factors++;
        result.setSimilarCasesScore(similarScore);

        // 计算总体置信度
        result.setTotalConfidence(totalScore);
        result.setConfidenceLevel(getConfidenceLevel(totalScore));
        result.setRecommendation(getRecommendation(totalScore, result));

        log.info("比赛 {} vs {} 置信度评估: {}分 - {}",
            analysis.getHomeTeam(), analysis.getAwayTeam(),
            totalScore, result.getConfidenceLevel());

        return result;
    }

    /**
     * 数据完整度评估
     */
    private int evaluateDataCompleteness(MatchAnalysis analysis) {
        int score = 0;

        // 赔率数据
        if (analysis.getHadLists() != null && !analysis.getHadLists().isEmpty()) {
            score += 4;
        }

        // 历史交锋
        if (analysis.getRecentMatches() != null && !analysis.getRecentMatches().isEmpty()) {
            score += 4;
        }

        // 同赔率比赛
        if (analysis.getSimilarMatches() != null && !analysis.getSimilarMatches().isEmpty()) {
            score += 4;
        }

        // xG数据
        if (analysis.getHomeTeamStats() != null) {
            score += 4;
        }
        if (analysis.getAwayTeamStats() != null) {
            score += 4;
        }

        return Math.min(score, 20);
    }

    /**
     * 赔率一致性评估
     */
    private int evaluateOddsConsistency(MatchAnalysis analysis, String aiPrediction) {
        if (analysis.getHadLists() == null || analysis.getHadLists().isEmpty()) {
            return 10; // 无数据时给中等分
        }

        try {
            var odds = analysis.getHadLists().getFirst();
            double h = Double.parseDouble(odds.getH());
            double d = Double.parseDouble(odds.getD());
            double a = Double.parseDouble(odds.getA());

            // 找出赔率最低的选项
            String oddsPreference;
            double lowestOdds = Math.min(Math.min(h, d), a);

            if (lowestOdds == h) {
                oddsPreference = "主胜";
            } else if (lowestOdds == a) {
                oddsPreference = "客胜";
            } else {
                oddsPreference = "平局";
            }

            // 如果AI预测与赔率倾向一致
            if (aiPrediction.contains(oddsPreference)) {
                // 计算赔率优势度
                double advantage = calculateOddsAdvantage(h, d, a, oddsPreference);
                if (advantage > 0.3) {
                    return 25; // 强烈一致
                } else if (advantage > 0.15) {
                    return 20; // 中度一致
                } else {
                    return 15; // 轻微一致
                }
            } else {
                // AI预测冷门
                return 8; // 冷门预测风险较高
            }
        } catch (Exception e) {
            return 10;
        }
    }

    private double calculateOddsAdvantage(double h, double d, double a, String preference) {
        double total = 1/h + 1/d + 1/a;
        double prob;
        switch (preference) {
            case "主胜" -> prob = (1/h) / total;
            case "客胜" -> prob = (1/a) / total;
            default -> prob = (1/d) / total;
        }
        // 与33%基准比较的优势
        return prob - 0.33;
    }

    /**
     * 历史验证评估 - 基于知识库中的历史准确率
     */
    private int evaluateHistoricalAccuracy(MatchAnalysis analysis) {
        // 检索相似比赛的历史预测准确率
        String query = String.format("联赛: %s 主队: %s 客队: %s",
            analysis.getLeague(), analysis.getHomeTeam(), analysis.getAwayTeam());

        List<MatchKnowledge> similarCases = vectorStore.searchSimilar(query, 10, 0.4);

        if (similarCases.isEmpty()) {
            return 12; // 无历史数据时给中等分
        }

        long correctCount = similarCases.stream()
            .filter(k -> Boolean.TRUE.equals(k.getPredictionCorrect()))
            .count();

        double accuracy = (double) correctCount / similarCases.size();

        if (accuracy >= 0.7) {
            return 25;
        } else if (accuracy >= 0.5) {
            return 18;
        } else if (accuracy >= 0.3) {
            return 12;
        } else {
            return 5;
        }
    }

    /**
     * xG数据支撑评估
     */
    private int evaluateXGSupport(MatchAnalysis analysis, String aiPrediction) {
        var homeStats = analysis.getHomeTeamStats();
        var awayStats = analysis.getAwayTeamStats();

        if (homeStats == null || awayStats == null) {
            return 7; // 无xG数据时给中等分
        }

        try {
            // 比较xG差值
            double homeXg = homeStats.getXG();
            double awayXg = awayStats.getXG();
            double homeXga = homeStats.getXGA();
            double awayXga = awayStats.getXGA();

            // 主队xG优势 = 主队预期进球 - 主队预期失球对比
            double homeAdvantage = (homeXg - homeXga) - (awayXg - awayXga);

            String xgPreference;
            if (homeAdvantage > 0.3) {
                xgPreference = "主胜";
            } else if (homeAdvantage < -0.3) {
                xgPreference = "客胜";
            } else {
                xgPreference = "平局";
            }

            // 检查AI预测是否与xG数据支撑一致
            if (aiPrediction.contains(xgPreference)) {
                double absAdvantage = Math.abs(homeAdvantage);
                if (absAdvantage > 0.5) {
                    return 15;
                } else if (absAdvantage > 0.3) {
                    return 12;
                } else {
                    return 10;
                }
            } else {
                return 5; // xG数据不支撑
            }
        } catch (Exception e) {
            return 7;
        }
    }

    /**
     * 相似案例准确率评估
     */
    private int evaluateSimilarCasesAccuracy(MatchAnalysis analysis) {
        if (analysis.getSimilarMatches() == null || analysis.getSimilarMatches().isEmpty()) {
            return 7;
        }

        // 基于同赔率比赛的结果分布评估
        // 这里假设analysis.getSimilarMatches()返回的是格式化的字符串
        // 在实际场景中需要解析数据计算
        return 10;
    }

    private String getConfidenceLevel(int score) {
        if (score >= 80) {
            return "高置信度";
        } else if (score >= 60) {
            return "中高置信度";
        } else if (score >= 40) {
            return "中置信度";
        } else if (score >= 20) {
            return "低置信度";
        } else {
            return "极低置信度";
        }
    }

    private String getRecommendation(int score, ConfidenceResult result) {
        StringBuilder sb = new StringBuilder();

        if (score >= 70) {
            sb.append("预测可信度较高，数据支撑充分。");
        } else if (score >= 50) {
            sb.append("预测可信度中等，建议关注以下因素：");
            if (result.getOddsConsistencyScore() < 15) {
                sb.append("\n- 赔率数据不支撑当前预测，可能存在冷门风险");
            }
            if (result.getXgSupportScore() < 10) {
                sb.append("\n- xG数据支撑不足");
            }
        } else {
            sb.append("预测可信度较低，数据支撑不足。");
            if (result.getDataCompletenessScore() < 12) {
                sb.append("\n- 数据不完整，需要更多信息");
            }
            if (result.getHistoricalScore() < 10) {
                sb.append("\n- 历史预测准确率较低");
            }
        }

        return sb.toString();
    }

    /**
     * 置信度评估结果
     */
    @lombok.Data
    public static class ConfidenceResult {
        private int totalConfidence;
        private String confidenceLevel;
        private int dataCompletenessScore;
        private int oddsConsistencyScore;
        private int historicalScore;
        private int xgSupportScore;
        private int similarCasesScore;
        private String recommendation;
    }
}
