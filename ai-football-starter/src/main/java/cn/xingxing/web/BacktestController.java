package cn.xingxing.web;

import cn.xingxing.dto.ApiResponse;
import cn.xingxing.entity.AiAnalysisResult;
import cn.xingxing.mapper.AiAnalysisResultMapper;
import cn.xingxing.rag.entity.PredictionStats;
import cn.xingxing.rag.service.BacktestService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 回测分析控制器
 * 提供预测准确率分析、错误模式分析等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/backtest")
@RequiredArgsConstructor
public class BacktestController {

    private final BacktestService backtestService;
    private final AiAnalysisResultMapper aiAnalysisResultMapper;

    /**
     * 生成完整回测报告
     */
    @GetMapping("/report")
    public ApiResponse<BacktestService.BacktestReport> getBacktestReport() {
        List<AiAnalysisResult> results = getCompletedResults();
        BacktestService.BacktestReport report = backtestService.generateBacktestReport(results);
        return ApiResponse.success(report);
    }

    /**
     * 生成每日统计数据
     */
    @GetMapping("/daily-stats")
    public ApiResponse<List<PredictionStats>> getDailyStats() {
        List<AiAnalysisResult> results = getCompletedResults();
        List<PredictionStats> stats = backtestService.generateDailyStats(results);
        return ApiResponse.success(stats);
    }

    /**
     * 分析预测错误模式
     */
    @GetMapping("/error-patterns")
    public ApiResponse<BacktestService.ErrorPatternAnalysis> getErrorPatterns() {
        List<AiAnalysisResult> results = getCompletedResults();
        BacktestService.ErrorPatternAnalysis analysis = backtestService.analyzeErrorPatterns(results);
        return ApiResponse.success(analysis);
    }

    /**
     * 获取准确率概览
     */
    @GetMapping("/summary")
    public ApiResponse<SummaryResponse> getSummary() {
        List<AiAnalysisResult> results = getCompletedResults();
        BacktestService.BacktestReport report = backtestService.generateBacktestReport(results);

        SummaryResponse summary = new SummaryResponse();
        summary.setTotalMatches(report.getTotalMatches());
        summary.setResultAccuracy(report.getResultAccuracy() != null ?
            report.getResultAccuracy().doubleValue() : 0);
        summary.setScoreAccuracy(report.getScoreAccuracy() != null ?
            report.getScoreAccuracy().doubleValue() : 0);
        summary.setRoi(report.getRoi() != null ?
            report.getRoi().doubleValue() : 0);

        if (report.getUpsetStats() != null) {
            summary.setUpsetAccuracy(report.getUpsetStats().getUpsetAccuracy() != null ?
                report.getUpsetStats().getUpsetAccuracy().doubleValue() : 0);
            summary.setTotalUpsets(report.getUpsetStats().getTotalUpsetPredictions());
            summary.setCorrectUpsets(report.getUpsetStats().getCorrectUpsetPredictions());
        }

        return ApiResponse.success(summary);
    }

    private List<AiAnalysisResult> getCompletedResults() {
        LambdaQueryWrapper<AiAnalysisResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(AiAnalysisResult::getMatchResult);
        queryWrapper.ne(AiAnalysisResult::getMatchResult, "");
        queryWrapper.isNotNull(AiAnalysisResult::getAiResult);
        return aiAnalysisResultMapper.selectList(queryWrapper);
    }

    @lombok.Data
    public static class SummaryResponse {
        private int totalMatches;
        private double resultAccuracy;
        private double scoreAccuracy;
        private double roi;
        private double upsetAccuracy;
        private int totalUpsets;
        private int correctUpsets;
    }
}
