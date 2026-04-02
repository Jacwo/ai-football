package cn.xingxing.service.impl;

import cn.xingxing.entity.AiAnalysisResult;
import cn.xingxing.entity.PredictionModel;
import cn.xingxing.entity.PredictionModelStats;
import cn.xingxing.mapper.AiAnalysisResultMapper;
import cn.xingxing.mapper.PredictionModelMapper;
import cn.xingxing.mapper.PredictionModelStatsMapper;
import cn.xingxing.service.PredictionModelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI预测模型Service实现类
 *
 * @Author: yangyuanliang
 * @Date: 2025-01-15
 * @Version: 1.0
 */
@Slf4j
@Service
public class PredictionModelServiceImpl extends ServiceImpl<PredictionModelMapper, PredictionModel>
        implements PredictionModelService {

    @Autowired
    private PredictionModelStatsMapper predictionModelStatsMapper;

    @Autowired
    private AiAnalysisResultMapper aiAnalysisResultMapper;

    @Override
    public List<PredictionModel> listActiveModels() {
        return baseMapper.selectActiveModels();
    }

    @Override
    public List<PredictionModel> listByModelType(String modelType) {
        return baseMapper.selectByModelType(modelType);
    }

    @Override
    public PredictionModel getByNameAndVersion(String modelName, String modelVersion) {
        return baseMapper.selectByNameAndVersion(modelName, modelVersion);
    }

    @Override
    public void calculateResultModelAccuracy(Integer sampleSize) {
        try {
            if (sampleSize == null || sampleSize <= 0) {
                sampleSize = 30; // 默认30场
            }

            log.info("开始计算胜负模型准确率，样本数量: {}", sampleSize);

            // 查询近N场有结果的预测记录
            List<AiAnalysisResult> recentMatches = aiAnalysisResultMapper.selectRecentPredictionsForResult(sampleSize);

            if (recentMatches.isEmpty()) {
                log.warn("没有找到可用的预测数据");
                return;
            }

            // 统计变量
            int totalPredictions = recentMatches.size();
            int correctPredictions = 0;
            int homeWinCount = 0;
            int homeWinCorrect = 0;
            int drawCount = 0;
            int drawCorrect = 0;
            int awayWinCount = 0;
            int awayWinCorrect = 0;

            // 遍历每场比赛进行统计
            for (AiAnalysisResult match : recentMatches) {
                String aiResult = match.getAiResult();
                String matchResult = match.getMatchResult();

                if (StringUtils.isBlank(aiResult) || StringUtils.isBlank(matchResult)) {
                    totalPredictions--;
                    continue;
                }

                // 解析实际比赛结果
                String actualResult = parseMatchResult(matchResult);
                if (actualResult == null) {
                    totalPredictions--;
                    continue;
                }

                // 统计各类预测
                switch (aiResult.trim()) {
                    case "胜":
                        homeWinCount++;
                        if (aiResult.trim().equals(actualResult)) {
                            homeWinCorrect++;
                            correctPredictions++;
                        }
                        break;
                    case "平":
                        drawCount++;
                        if (aiResult.trim().equals(actualResult)) {
                            drawCorrect++;
                            correctPredictions++;
                        }
                        break;
                    case "负":
                        awayWinCount++;
                        if (aiResult.trim().equals(actualResult)) {
                            awayWinCorrect++;
                            correctPredictions++;
                        }
                        break;
                    default:
                        totalPredictions--;
                        break;
                }
            }

            // 计算准确率
            BigDecimal accuracyRate = BigDecimal.ZERO;
            if (totalPredictions > 0) {
                accuracyRate = BigDecimal.valueOf(correctPredictions)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalPredictions), 2, RoundingMode.HALF_UP);
            }

            // 保存统计结果
            PredictionModelStats stats = new PredictionModelStats();
            stats.setModelId("MODEL_RESULT_V1");
            stats.setModelName("胜负模型v1");
            stats.setModelType("result");
            stats.setStatsDate(LocalDateTime.now());
            stats.setSampleSize(sampleSize);
            stats.setTotalPredictions(totalPredictions);
            stats.setCorrectPredictions(correctPredictions);
            stats.setAccuracyRate(accuracyRate);
            stats.setHomeWinCount(homeWinCount);
            stats.setHomeWinCorrect(homeWinCorrect);
            stats.setDrawCount(drawCount);
            stats.setDrawCorrect(drawCorrect);
            stats.setAwayWinCount(awayWinCount);
            stats.setAwayWinCorrect(awayWinCorrect);

            predictionModelStatsMapper.insert(stats);

            log.info("胜负模型准确率计算完成 - 总场次: {}, 正确: {}, 准确率: {}%",
                    totalPredictions, correctPredictions, accuracyRate);
        } catch (Exception e) {
            log.error("胜负模型准确率计算失败", e);
            throw new RuntimeException("胜负模型准确率计算失败: " + e.getMessage());
        }
    }

    @Override
    public void calculateScoreModelAccuracy(Integer sampleSize) {
        try {
            if (sampleSize == null || sampleSize <= 0) {
                sampleSize = 30; // 默认30场
            }

            log.info("开始计算比分模型准确率，样本数量: {}", sampleSize);

            // 查询近N场有比分预测结果的记录
            List<AiAnalysisResult> recentMatches = aiAnalysisResultMapper.selectRecentPredictionsForScore(sampleSize);

            if (recentMatches.isEmpty()) {
                log.warn("没有找到可用的比分预测数据");
                return;
            }

            // 统计变量
            int totalPredictions = recentMatches.size();
            int correctPredictions = 0;

            // 遍历每场比赛进行统计
            for (AiAnalysisResult match : recentMatches) {
                String aiScore = match.getAiScore();
                String matchResult = match.getMatchResult();

                if (StringUtils.isBlank(aiScore) || StringUtils.isBlank(matchResult)) {
                    totalPredictions--;
                    continue;
                }

                // 比分完全匹配才算正确
                if (aiScore.trim().equals(matchResult.trim())) {
                    correctPredictions++;
                }
            }

            // 计算准确率
            BigDecimal accuracyRate = BigDecimal.ZERO;
            if (totalPredictions > 0) {
                accuracyRate = BigDecimal.valueOf(correctPredictions)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalPredictions), 2, RoundingMode.HALF_UP);
            }

            // 保存统计结果
            PredictionModelStats stats = new PredictionModelStats();
            stats.setModelId("MODEL_SCORE_V1");
            stats.setModelName("比分模型v1");
            stats.setModelType("score");
            stats.setStatsDate(LocalDateTime.now());
            stats.setSampleSize(sampleSize);
            stats.setTotalPredictions(totalPredictions);
            stats.setCorrectPredictions(correctPredictions);
            stats.setAccuracyRate(accuracyRate);

            predictionModelStatsMapper.insert(stats);

            log.info("比分模型准确率计算完成 - 总场次: {}, 正确: {}, 准确率: {}%",
                    totalPredictions, correctPredictions, accuracyRate);
        } catch (Exception e) {
            log.error("比分模型准确率计算失败", e);
            throw new RuntimeException("比分模型准确率计算失败: " + e.getMessage());
        }
    }

    @Override
    public void calculateAllModelsAccuracy(Integer sampleSize) {
        log.info("开始计算所有模型准确率，样本数量: {}", sampleSize);
        calculateResultModelAccuracy(sampleSize);
        calculateScoreModelAccuracy(sampleSize);
        log.info("所有模型准确率计算完成");
    }

    @Override
    public PredictionModelStats getLatestStats(String modelId) {
        return predictionModelStatsMapper.selectLatestByModelId(modelId);
    }

    @Override
    public PredictionModelStats getLatestStatsByName(String modelName) {
        return predictionModelStatsMapper.selectLatestByModelName(modelName);
    }

    @Override
    public List<Map<String, Object>> getAllLatestStats() {
        // 查询胜负模型统计
        PredictionModelStats resultStats = getLatestStatsByName("胜负模型v1");
        // 查询比分模型统计
        PredictionModelStats scoreStats = getLatestStatsByName("比分模型v1");

        return List.of(
                buildStatsMap(resultStats),
                buildStatsMap(scoreStats)
        );
    }

    @Override
    public List<PredictionModelStats> getHistoryStats(String modelId, LocalDateTime startDate, LocalDateTime endDate) {
        return predictionModelStatsMapper.selectHistoryByModelId(modelId, startDate, endDate);
    }

    @Override
    public List<PredictionModelStats> getLatestStatsByType(String modelType) {
        return predictionModelStatsMapper.selectLatestByModelType(modelType);
    }

    /**
     * 解析比赛结果为胜平负
     */
    private String parseMatchResult(String matchResult) {
        try {
            if (!matchResult.matches("^[0-9]+:[0-9]+$")) {
                return null;
            }

            String[] scores = matchResult.split(":");
            int homeScore = Integer.parseInt(scores[0].trim());
            int awayScore = Integer.parseInt(scores[1].trim());

            if (homeScore > awayScore) {
                return "胜";
            } else if (homeScore == awayScore) {
                return "平";
            } else {
                return "负";
            }
        } catch (Exception e) {
            log.error("解析比赛结果失败: {}", matchResult, e);
            return null;
        }
    }

    /**
     * 构建统计数据Map
     */
    private Map<String, Object> buildStatsMap(PredictionModelStats stats) {
        Map<String, Object> map = new HashMap<>();
        if (stats == null) {
            return map;
        }

        map.put("modelName", stats.getModelName());
        map.put("modelType", stats.getModelType());
        map.put("totalPredictions", stats.getTotalPredictions());
        map.put("correctPredictions", stats.getCorrectPredictions());
        map.put("accuracyRate", stats.getAccuracyRate());
        map.put("sampleSize", stats.getSampleSize());
        map.put("statsDate", stats.getStatsDate());

        if ("result".equals(stats.getModelType())) {
            map.put("homeWin", String.format("%d/%d", stats.getHomeWinCorrect(), stats.getHomeWinCount()));
            map.put("draw", String.format("%d/%d", stats.getDrawCorrect(), stats.getDrawCount()));
            map.put("awayWin", String.format("%d/%d", stats.getAwayWinCorrect(), stats.getAwayWinCount()));
        }

        return map;
    }
}
