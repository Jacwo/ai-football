package cn.xingxing.web;

import cn.xingxing.dto.ApiResponse;
import cn.xingxing.entity.PredictionModel;
import cn.xingxing.entity.PredictionModelStats;
import cn.xingxing.service.PredictionModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI预测模型Controller
 *
 * @Author: yangyuanliang
 * @Date: 2025-01-15
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/prediction-model")
public class PredictionModelController {

    @Autowired
    private PredictionModelService predictionModelService;

    /**
     * 查询所有启用的模型
     */
    @GetMapping("/list")
    public ApiResponse<List<PredictionModel>> listActiveModels() {
        List<PredictionModel> models = predictionModelService.listActiveModels();
        return ApiResponse.success(models);
    }

    /**
     * 根据模型类型查询模型
     */
    @GetMapping("/type/{modelType}")
    public ApiResponse<List<PredictionModel>> listByModelType(@PathVariable String modelType) {
        List<PredictionModel> models = predictionModelService.listByModelType(modelType);
        return ApiResponse.success(models);
    }

    /**
     * 执行胜负模型准确率计算
     */
    @PostMapping("/calculate/result")
    public ApiResponse<String> calculateResultModelAccuracy() {
        try {
            predictionModelService.calculateResultModelAccuracy();
            return ApiResponse.success("胜负模型准确率计算成功");
        } catch (Exception e) {
            log.error("胜负模型准确率计算失败", e);
            return ApiResponse.error("胜负模型准确率计算失败: " + e.getMessage());
        }
    }

    /**
     * 执行比分模型准确率计算
     */
    @PostMapping("/calculate/score")
    public ApiResponse<String> calculateScoreModelAccuracy() {
        try {
            predictionModelService.calculateScoreModelAccuracy();
            return ApiResponse.success("比分模型准确率计算成功");
        } catch (Exception e) {
            log.error("比分模型准确率计算失败", e);
            return ApiResponse.error("比分模型准确率计算失败: " + e.getMessage());
        }
    }

    /**
     * 执行所有模型准确率计算
     */
    @PostMapping("/calculate/all")
    public ApiResponse<String> calculateAllModelsAccuracy() {
        try {
            predictionModelService.calculateAllModelsAccuracy();
            return ApiResponse.success("所有模型准确率计算成功");
        } catch (Exception e) {
            log.error("所有模型准确率计算失败", e);
            return ApiResponse.error("所有模型准确率计算失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有模型的最新统计
     */
    @GetMapping("/stats/latest")
    public ApiResponse<List<Map<String, Object>>> getAllLatestStats() {
        List<Map<String, Object>> stats = predictionModelService.getAllLatestStats();
        return ApiResponse.success(stats);
    }

    /**
     * 查询模型的最新统计数据（按模型名称）
     */
    @GetMapping("/stats/latest/{modelName}")
    public ApiResponse<PredictionModelStats> getLatestStatsByName(@PathVariable String modelName) {
        PredictionModelStats stats = predictionModelService.getLatestStatsByName(modelName);
        return ApiResponse.success(stats);
    }

    /**
     * 查询指定类型模型的最新统计
     */
    @GetMapping("/stats/type/{modelType}")
    public ApiResponse<List<PredictionModelStats>> getLatestStatsByType(@PathVariable String modelType) {
        List<PredictionModelStats> stats = predictionModelService.getLatestStatsByType(modelType);
        return ApiResponse.success(stats);
    }

    /**
     * 查询模型的历史统计数据
     */
    @GetMapping("/stats/history/{modelId}")
    public ApiResponse<List<PredictionModelStats>> getHistoryStats(
            @PathVariable String modelId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        List<PredictionModelStats> stats = predictionModelService.getHistoryStats(modelId, startDate, endDate);
        return ApiResponse.success(stats);
    }

    /**
     * 查询模型统计汇总（包含胜负模型和比分模型的最新准确率）
     */
    @GetMapping("/stats/summary")
    public ApiResponse<Map<String, Object>> getStatsSummary() {
        Map<String, Object> summary = new HashMap<>();

        // 获取胜负模型统计
        PredictionModelStats resultStats = predictionModelService.getLatestStatsByName("胜负模型v1");
        if (resultStats != null) {
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("modelName", resultStats.getModelName());
            resultData.put("totalPredictions", resultStats.getTotalPredictions());
            resultData.put("correctPredictions", resultStats.getCorrectPredictions());
            resultData.put("accuracyRate", resultStats.getAccuracyRate());
            resultData.put("recent10Accuracy", resultStats.getRecent10Accuracy());
            resultData.put("recent20Accuracy", resultStats.getRecent20Accuracy());
            resultData.put("recent50Accuracy", resultStats.getRecent50Accuracy());
            resultData.put("homeWin", String.format("%d/%d", resultStats.getHomeWinCorrect(), resultStats.getHomeWinCount()));
            resultData.put("draw", String.format("%d/%d", resultStats.getDrawCorrect(), resultStats.getDrawCount()));
            resultData.put("awayWin", String.format("%d/%d", resultStats.getAwayWinCorrect(), resultStats.getAwayWinCount()));
            resultData.put("statsDate", resultStats.getStatsDate());
            summary.put("resultModel", resultData);
        }

        // 获取比分模型统计
        PredictionModelStats scoreStats = predictionModelService.getLatestStatsByName("比分模型v1");
        if (scoreStats != null) {
            Map<String, Object> scoreData = new HashMap<>();
            scoreData.put("modelName", scoreStats.getModelName());
            scoreData.put("totalPredictions", scoreStats.getTotalPredictions());
            scoreData.put("correctPredictions", scoreStats.getCorrectPredictions());
            scoreData.put("accuracyRate", scoreStats.getAccuracyRate());
            scoreData.put("recent10Accuracy", scoreStats.getRecent10Accuracy());
            scoreData.put("recent20Accuracy", scoreStats.getRecent20Accuracy());
            scoreData.put("recent50Accuracy", scoreStats.getRecent50Accuracy());
            scoreData.put("statsDate", scoreStats.getStatsDate());
            summary.put("scoreModel", scoreData);
        }

        return ApiResponse.success(summary);
    }
}
