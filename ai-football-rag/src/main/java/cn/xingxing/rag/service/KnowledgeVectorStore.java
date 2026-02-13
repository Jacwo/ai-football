package cn.xingxing.rag.service;

import cn.xingxing.rag.entity.MatchKnowledge;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 知识库向量存储服务
 * 使用本地嵌入模型和内存向量存储，支持语义检索历史分析经验
 */
@Slf4j
@Service
public class KnowledgeVectorStore {

    private EmbeddingModel embeddingModel;
    private EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 存储matchId到知识的映射，便于快速查找
     */
    private final Map<String, MatchKnowledge> knowledgeMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 使用轻量级本地嵌入模型
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        this.embeddingStore = new InMemoryEmbeddingStore<>();
        log.info("知识库向量存储服务初始化完成");
    }

    /**
     * 将比赛知识添加到向量存储
     */
    public void addKnowledge(MatchKnowledge knowledge) {
        if (knowledge == null || knowledge.getAnalysisSummary() == null) {
            return;
        }

        // 构建用于检索的文本
        String searchText = buildSearchText(knowledge);

        // 创建带元数据的文本片段
        Metadata metadata = Metadata.from(Map.of(
            "matchId", knowledge.getMatchId(),
            "league", knowledge.getLeague() != null ? knowledge.getLeague() : "",
            "homeTeam", knowledge.getHomeTeam(),
            "awayTeam", knowledge.getAwayTeam(),
            "predictionCorrect", String.valueOf(knowledge.getPredictionCorrect()),
            "scenarioTags", knowledge.getScenarioTags() != null ? knowledge.getScenarioTags() : ""
        ));

        TextSegment segment = TextSegment.from(searchText, metadata);

        // 生成嵌入向量并存储
        Embedding embedding = embeddingModel.embed(segment).content();
        embeddingStore.add(embedding, segment);

        // 保存到映射
        knowledgeMap.put(knowledge.getMatchId(), knowledge);

        log.debug("已添加比赛知识: {} vs {}", knowledge.getHomeTeam(), knowledge.getAwayTeam());
    }

    /**
     * 批量添加知识
     */
    public void addKnowledgeBatch(List<MatchKnowledge> knowledgeList) {
        knowledgeList.forEach(this::addKnowledge);
        log.info("批量添加 {} 条比赛知识", knowledgeList.size());
    }

    /**
     * 语义检索相似的历史分析经验
     *
     * @param query 查询文本（当前比赛的分析特征描述）
     * @param maxResults 最大返回结果数
     * @param minScore 最小相似度分数 (0-1)
     * @return 相似的历史知识列表
     */
    public List<MatchKnowledge> searchSimilar(String query, int maxResults, double minScore) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(maxResults)
            .minScore(minScore)
            .build();

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

        return result.matches().stream()
            .map(this::toMatchKnowledge)
            .filter(k -> k != null)
            .toList();
    }

    /**
     * 根据联赛和场景标签检索
     */
    public List<MatchKnowledge> searchByLeagueAndScenario(String league, String scenario, int maxResults) {
        String query = String.format("联赛: %s 场景: %s", league, scenario);
        return searchSimilar(query, maxResults, 0.5);
    }

    /**
     * 检索预测正确的相似案例
     */
    public List<MatchKnowledge> searchCorrectPredictions(String query, int maxResults) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(maxResults * 2) // 多检索一些再过滤
            .minScore(0.5)
            .build();

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

        return result.matches().stream()
            .map(this::toMatchKnowledge)
            .filter(k -> k != null && Boolean.TRUE.equals(k.getPredictionCorrect()))
            .limit(maxResults)
            .toList();
    }

    /**
     * 获取知识库大小
     */
    public int getKnowledgeSize() {
        return knowledgeMap.size();
    }

    /**
     * 清空知识库（重新初始化时使用）
     */
    public void clear() {
        this.embeddingStore = new InMemoryEmbeddingStore<>();
        this.knowledgeMap.clear();
        log.info("知识库已清空");
    }

    private String buildSearchText(MatchKnowledge knowledge) {
        StringBuilder sb = new StringBuilder();
        sb.append("联赛: ").append(knowledge.getLeague()).append(" ");
        sb.append("主队: ").append(knowledge.getHomeTeam()).append(" ");
        sb.append("客队: ").append(knowledge.getAwayTeam()).append(" ");

        if (knowledge.getScenarioTags() != null) {
            sb.append("场景: ").append(knowledge.getScenarioTags()).append(" ");
        }

        if (knowledge.getAnalysisSummary() != null) {
            sb.append("分析: ").append(knowledge.getAnalysisSummary()).append(" ");
        }

        if (knowledge.getLearningInsight() != null) {
            sb.append("经验: ").append(knowledge.getLearningInsight());
        }

        return sb.toString();
    }

    private MatchKnowledge toMatchKnowledge(EmbeddingMatch<TextSegment> match) {
        String matchId = match.embedded().metadata().getString("matchId");
        return knowledgeMap.get(matchId);
    }
}
