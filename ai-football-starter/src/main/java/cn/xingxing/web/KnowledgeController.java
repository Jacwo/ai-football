package cn.xingxing.web;

import cn.xingxing.dto.ApiResponse;
import cn.xingxing.rag.service.BacktestService;
import cn.xingxing.rag.service.KnowledgeInitService;
import cn.xingxing.rag.service.RAGService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 知识库管理控制器
 * 提供知识库统计、刷新、回测等功能接口
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeInitService knowledgeInitService;
    private final RAGService ragService;
    private final BacktestService backtestService;

    /**
     * 获取知识库统计信息
     */
    @GetMapping("/stats")
    public ApiResponse<KnowledgeInitService.KnowledgeStats> getStats() {
        KnowledgeInitService.KnowledgeStats stats = knowledgeInitService.getStats();
        return ApiResponse.success(stats);
    }

    /**
     * 刷新知识库
     */
    @PostMapping("/refresh")
    public ApiResponse<String> refreshKnowledge() {
        String result = knowledgeInitService.refreshKnowledgeBase();
        return ApiResponse.success(result);
    }

    /**
     * 获取知识库简要信息
     */
    @GetMapping("/info")
    public ApiResponse<String> getInfo() {
        return ApiResponse.success(ragService.getKnowledgeStats());
    }

    /**
     * 查询相似历史案例
     */
    @GetMapping("/similar")
    public ApiResponse<String> getSimilarCases(
            @RequestParam String league,
            @RequestParam String homeTeam,
            @RequestParam String awayTeam,
            @RequestParam(required = false) String scenario) {
        String experience = ragService.retrieveRelevantExperience(
            league, homeTeam, awayTeam, "", scenario);
        return ApiResponse.success(experience);
    }

    /**
     * 获取联赛成功预测案例
     */
    @GetMapping("/success/{league}")
    public ApiResponse<String> getSuccessfulPredictions(
            @PathVariable String league,
            @RequestParam(defaultValue = "5") int limit) {
        String predictions = ragService.retrieveSuccessfulPredictions(league, limit);
        return ApiResponse.success(predictions);
    }
}
