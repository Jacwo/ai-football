package cn.xingxing.service.impl;

import cn.xingxing.entity.PredictionModel;
import cn.xingxing.entity.PredictionModelStats;
import cn.xingxing.mapper.PredictionModelMapper;
import cn.xingxing.mapper.PredictionModelStatsMapper;
import cn.xingxing.service.PredictionModelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    public void calculateResultModelAccuracy() {
        try {
            log.info("开始计算胜负模型准确率...");
            predictionModelStatsMapper.calculateResultModelAccuracy();
            log.info("胜负模型准确率计算完成");
        } catch (Exception e) {
            log.error("胜负模型准确率计算失败", e);
            throw new RuntimeException("胜负模型准确率计算失败: " + e.getMessage());
        }
    }

    @Override
    public void calculateScoreModelAccuracy() {
        try {
            log.info("开始计算比分模型准确率...");
            predictionModelStatsMapper.calculateScoreModelAccuracy();
            log.info("比分模型准确率计算完成");
        } catch (Exception e) {
            log.error("比分模型准确率计算失败", e);
            throw new RuntimeException("比分模型准确率计算失败: " + e.getMessage());
        }
    }

    @Override
    public void calculateAllModelsAccuracy() {
        log.info("开始计算所有模型准确率...");
        calculateResultModelAccuracy();
        calculateScoreModelAccuracy();
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
        return predictionModelStatsMapper.selectAllLatestStats();
    }

    @Override
    public List<PredictionModelStats> getHistoryStats(String modelId, LocalDateTime startDate, LocalDateTime endDate) {
        return predictionModelStatsMapper.selectHistoryByModelId(modelId, startDate, endDate);
    }

    @Override
    public List<PredictionModelStats> getLatestStatsByType(String modelType) {
        return predictionModelStatsMapper.selectLatestByModelType(modelType);
    }
}
