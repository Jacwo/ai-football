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
     * @param sampleSize 样本数量（可选，默认30场）
     */
    @PostMapping("/calculate/result")
    public ApiResponse<String> calculateResultModelAccuracy(
            @RequestParam(required = false, defaultValue = "30") Integer sampleSize) {
        try {
            predictionModelService.calculateResultModelAccuracy(sampleSize);
            return ApiResponse.success("胜负模型准确率计算成功");
        } catch (Exception e) {
            log.error("胜负模型准确率计算失败", e);
            return ApiResponse.error("胜负模型准确率计算失败: " + e.getMessage());
        }
    }

    /**
     * 执行比分模型准确率计算
     * @param sampleSize 样本数量（可选，默认30场）
     */
    @PostMapping("/calculate/score")
    public ApiResponse<String> calculateScoreModelAccuracy(
            @RequestParam(required = false, defaultValue = "30") Integer sampleSize) {
        try {
            predictionModelService.calculateScoreModelAccuracy(sampleSize);
            return ApiResponse.success("比分模型准确率计算成功");
        } catch (Exception e) {
            log.error("比分模型准确率计算失败", e);
            return ApiResponse.error("比分模型准确率计算失败: " + e.getMessage());
        }
    }

    /**
     * 执行所有模型准确率计算
     * @param sampleSize 样本数量（可选，默认30场）
     */
    @PostMapping("/calculate/all")
    public ApiResponse<String> calculateAllModelsAccuracy(
            @RequestParam(required = false, defaultValue = "30") Integer sampleSize) {
        try {
            predictionModelService.calculateAllModelsAccuracy(sampleSize);
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
}
