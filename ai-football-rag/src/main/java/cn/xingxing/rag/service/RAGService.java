package cn.xingxing.rag.service;

import cn.xingxing.rag.entity.MatchKnowledge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG检索增强服务
 * 负责从知识库中检索相关历史分析经验，增强AI预测能力
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RAGService {

    private final KnowledgeVectorStore vectorStore;

    /**
     * 检索与当前比赛相似的历史分析经验
     *
     * @param league 联赛
     * @param homeTeam 主队
     * @param awayTeam 客队
     * @param oddsInfo 赔率信息
     * @param scenario 比赛场景描述
     * @return 格式化的历史经验文本，可直接嵌入到提示词中
     */
    public String retrieveRelevantExperience(String league, String homeTeam, String awayTeam,
                                              String oddsInfo, String scenario) {
        // 构建查询文本
        String query = buildQuery(league, homeTeam, awayTeam, oddsInfo, scenario);

        // 检索相似的历史案例
        List<MatchKnowledge> similarCases = vectorStore.searchSimilar(query, 5, 0.4);

        if (similarCases.isEmpty()) {
            log.info("未找到相似的历史分析经验");
            return "";
        }

        // 格式化历史经验
        return formatExperience(similarCases);
    }

    /**
     * 检索特定联赛的预测正确案例
     */
    public String retrieveSuccessfulPredictions(String league, int limit) {
        String query = String.format("联赛: %s 预测准确", league);
        List<MatchKnowledge> correctCases = vectorStore.searchCorrectPredictions(query, limit);

        if (correctCases.isEmpty()) {
            return "";
        }

        return formatSuccessfulPredictions(correctCases);
    }

    /**
     * 检索特定场景的历史经验
     */
    public String retrieveScenarioExperience(String scenario) {
        List<MatchKnowledge> cases = vectorStore.searchSimilar(scenario, 3, 0.5);

        if (cases.isEmpty()) {
            return "";
        }

        return formatScenarioExperience(cases);
    }

    /**
     * 构建增强的分析提示词
     * 将历史经验注入到原始提示词中
     */
    public String buildEnhancedPrompt(String originalPrompt, String league, String homeTeam,
                                       String awayTeam, String oddsInfo, String scenario) {
        String experience = retrieveRelevantExperience(league, homeTeam, awayTeam, oddsInfo, scenario);
        String successfulCases = retrieveSuccessfulPredictions(league, 3);

        if (experience.isEmpty() && successfulCases.isEmpty()) {
            return originalPrompt;
        }

        StringBuilder enhancedPrompt = new StringBuilder(originalPrompt);
        enhancedPrompt.append("\n\n---\n");
        enhancedPrompt.append("**历史分析经验参考（RAG增强）**\n");

        if (!experience.isEmpty()) {
            enhancedPrompt.append("\n相似比赛的历史分析经验：\n");
            enhancedPrompt.append(experience);
        }

        if (!successfulCases.isEmpty()) {
            enhancedPrompt.append("\n成功预测的历史案例：\n");
            enhancedPrompt.append(successfulCases);
        }

        enhancedPrompt.append("\n请参考以上历史经验，结合当前比赛数据，给出更准确的预测。");
        enhancedPrompt.append("\n注意：历史经验仅供参考，需要结合当前比赛的具体情况进行分析。");

        return enhancedPrompt.toString();
    }

    /**
     * 获取知识库统计信息
     */
    public String getKnowledgeStats() {
        int size = vectorStore.getKnowledgeSize();
        return String.format("知识库当前包含 %d 条历史分析经验", size);
    }

    private String buildQuery(String league, String homeTeam, String awayTeam,
                              String oddsInfo, String scenario) {
        StringBuilder query = new StringBuilder();
        query.append("联赛: ").append(league).append(" ");
        query.append("主队: ").append(homeTeam).append(" ");
        query.append("客队: ").append(awayTeam).append(" ");

        if (oddsInfo != null && !oddsInfo.isEmpty()) {
            query.append("赔率特征: ").append(oddsInfo).append(" ");
        }

        if (scenario != null && !scenario.isEmpty()) {
            query.append("比赛场景: ").append(scenario);
        }

        return query.toString();
    }

    private String formatExperience(List<MatchKnowledge> cases) {
        return cases.stream()
            .map(this::formatSingleExperience)
            .collect(Collectors.joining("\n"));
    }

    private String formatSingleExperience(MatchKnowledge knowledge) {
        StringBuilder sb = new StringBuilder();
        sb.append("- [").append(knowledge.getLeague()).append("] ");
        sb.append(knowledge.getHomeTeam()).append(" vs ").append(knowledge.getAwayTeam());
        sb.append(" | 预测: ").append(knowledge.getAiPrediction());
        sb.append(" | 实际: ").append(knowledge.getActualResult());
        sb.append(" | ").append(Boolean.TRUE.equals(knowledge.getPredictionCorrect()) ? "✓正确" : "✗错误");

        if (knowledge.getLearningInsight() != null && !knowledge.getLearningInsight().isEmpty()) {
            sb.append("\n  经验总结: ").append(truncate(knowledge.getLearningInsight(), 100));
        }

        return sb.toString();
    }

    private String formatSuccessfulPredictions(List<MatchKnowledge> cases) {
        return cases.stream()
            .map(k -> String.format("- [%s] %s vs %s: 预测%s(%s), 实际%s - 关键因素: %s",
                k.getLeague(),
                k.getHomeTeam(),
                k.getAwayTeam(),
                k.getAiPrediction(),
                k.getAiScore(),
                k.getActualResult(),
                truncate(k.getKeyFeatures(), 80)))
            .collect(Collectors.joining("\n"));
    }

    private String formatScenarioExperience(List<MatchKnowledge> cases) {
        return cases.stream()
            .map(k -> String.format("场景[%s]: %s vs %s - %s",
                k.getScenarioTags(),
                k.getHomeTeam(),
                k.getAwayTeam(),
                k.getLearningInsight()))
            .collect(Collectors.joining("\n"));
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
