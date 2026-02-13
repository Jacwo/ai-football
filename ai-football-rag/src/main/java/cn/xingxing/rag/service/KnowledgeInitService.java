package cn.xingxing.rag.service;

import cn.xingxing.entity.AiAnalysisResult;
import cn.xingxing.mapper.AiAnalysisResultMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识库初始化服务
 * 应用启动时自动加载历史分析数据到向量存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeInitService {

    private final AiAnalysisResultMapper aiAnalysisResultMapper;
    private final BacktestService backtestService;
    private final KnowledgeVectorStore vectorStore;

    /**
     * 应用启动后异步初始化知识库
     */
    @PostConstruct
    @Async
    public void initKnowledgeBase() {
        log.info("开始初始化AI预测知识库...");

        try {
            // 首先尝试从数据库加载已有的知识
            int dbCount = backtestService.loadKnowledgeFromDatabase();

            if (dbCount > 0) {
                log.info("从数据库加载了 {} 条知识", dbCount);
            }

            // 查询所有有结果的历史分析记录，同步新数据
            LambdaQueryWrapper<AiAnalysisResult> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.isNotNull(AiAnalysisResult::getMatchResult);
            queryWrapper.ne(AiAnalysisResult::getMatchResult, "");
            queryWrapper.isNotNull(AiAnalysisResult::getAiResult);

            List<AiAnalysisResult> historicalResults = aiAnalysisResultMapper.selectList(queryWrapper);

            if (historicalResults.isEmpty()) {
                log.info("暂无历史分析数据");
                return;
            }

            // 构建/更新知识库（会同步到数据库）
            int count = backtestService.buildKnowledgeFromHistory(historicalResults);

            log.info("知识库初始化完成，共 {} 条历史分析经验", count);

            // 生成回测报告
            var report = backtestService.generateBacktestReport(historicalResults);
            log.info("历史预测准确率统计: 总场次={}, 胜负准确率={}%, 比分准确率={}%",
                report.getTotalMatches(),
                report.getResultAccuracy(),
                report.getScoreAccuracy());

            // 保存每日统计到数据库
            var dailyStats = backtestService.generateDailyStats(historicalResults);
            backtestService.saveDailyStats(dailyStats);

        } catch (Exception e) {
            log.error("知识库初始化失败", e);
        }
    }

    /**
     * 手动刷新知识库
     */
    public String refreshKnowledgeBase() {
        log.info("手动刷新知识库...");

        // 清空现有知识库
        vectorStore.clear();

        // 重新初始化
        initKnowledgeBase();

        return String.format("知识库刷新完成，当前包含 %d 条知识", vectorStore.getKnowledgeSize());
    }

    /**
     * 增量更新知识库 - 添加新的分析结果
     */
    public void addNewKnowledge(AiAnalysisResult result) {
        if (result.getMatchResult() != null && !result.getMatchResult().isEmpty()) {
            backtestService.buildKnowledgeFromHistory(List.of(result));
            log.info("新增知识: {} vs {}", result.getHomeTeam(), result.getAwayTeam());
        }
    }

    /**
     * 获取知识库统计信息
     */
    public KnowledgeStats getStats() {
        KnowledgeStats stats = new KnowledgeStats();
        stats.setTotalKnowledge(vectorStore.getKnowledgeSize());

        // 获取回测报告
        LambdaQueryWrapper<AiAnalysisResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(AiAnalysisResult::getMatchResult);
        queryWrapper.ne(AiAnalysisResult::getMatchResult, "");
        List<AiAnalysisResult> results = aiAnalysisResultMapper.selectList(queryWrapper);

        if (!results.isEmpty()) {
            var report = backtestService.generateBacktestReport(results);
            stats.setTotalPredictions(report.getTotalMatches());
            stats.setResultAccuracy(report.getResultAccuracy().doubleValue());
            stats.setScoreAccuracy(report.getScoreAccuracy().doubleValue());
            stats.setRoi(report.getRoi() != null ? report.getRoi().doubleValue() : 0.0);
        }

        return stats;
    }

    @lombok.Data
    public static class KnowledgeStats {
        private int totalKnowledge;
        private int totalPredictions;
        private double resultAccuracy;
        private double scoreAccuracy;
        private double roi;
    }
}
