package cn.xingxing.service;

import cn.xingxing.entity.PredictionModel;
import cn.xingxing.entity.PredictionModelStats;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI预测模型Service
 *
 * @Author: yangyuanliang
 * @Date: 2025-01-15
 * @Version: 1.0
 */
public interface PredictionModelService extends IService<PredictionModel> {

    /**
     * 查询所有启用的模型
     */
    List<PredictionModel> listActiveModels();

    /**
     * 根据模型类型查询模型
     */
    List<PredictionModel> listByModelType(String modelType);

    /**
     * 根据模型名称和版本查询模型
     */
    PredictionModel getByNameAndVersion(String modelName, String modelVersion);

    /**
     * 执行胜负模型准确率计算
     */
    void calculateResultModelAccuracy();

    /**
     * 执行比分模型准确率计算
     */
    void calculateScoreModelAccuracy();

    /**
     * 执行所有模型准确率计算
     */
    void calculateAllModelsAccuracy();

    /**
     * 查询模型的最新统计数据
     */
    PredictionModelStats getLatestStats(String modelId);

    /**
     * 查询模型的最新统计数据（按模型名称）
     */
    PredictionModelStats getLatestStatsByName(String modelName);

    /**
     * 查询所有模型的最新统计
     */
    List<Map<String, Object>> getAllLatestStats();

    /**
     * 查询模型的历史统计数据
     */
    List<PredictionModelStats> getHistoryStats(String modelId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 查询指定类型模型的最新统计
     */
    List<PredictionModelStats> getLatestStatsByType(String modelType);
}
