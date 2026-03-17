package cn.xingxing.rag.service;

import cn.xingxing.dto.MatchAnalysis;
import cn.xingxing.entity.HadList;
import cn.xingxing.rag.entity.MatchKnowledge;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 增强版AI分析服务
 * 整合RAG检索、置信度评估，提供更精准的足球预测
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedAIService {

    private final RAGService ragService;
    private final ConfidenceEvaluator confidenceEvaluator;
    private final KnowledgeVectorStore knowledgeVectorStore;

    // 支持xG数据的五大联赛
    private static final List<String> XG_LEAGUES = List.of("意甲", "英超", "西甲", "德甲", "法甲");

    /**
     * 构建RAG增强的分析提示词
     */
    public String buildEnhancedPrompt(MatchAnalysis analysis) {
        String basePrompt;
        if (XG_LEAGUES.contains(analysis.getLeague())) {
            basePrompt = buildPromptWithXg(analysis);
        } else {
            basePrompt = buildBasePrompt(analysis);
        }

        // RAG增强：检索相似历史案例
        String scenario = inferScenario(analysis);
        String enhancedPrompt = ragService.buildEnhancedPrompt(
            basePrompt,
            analysis.getLeague(),
            analysis.getHomeTeam(),
            analysis.getAwayTeam(),
            formatOddsInfo(analysis),
            scenario
        );

        return enhancedPrompt;
    }

    /**
     * 评估预测置信度
     */
    public ConfidenceEvaluator.ConfidenceResult evaluateConfidence(MatchAnalysis analysis, String aiPrediction) {
        return confidenceEvaluator.evaluate(analysis, aiPrediction);
    }

    /**
     * 获取历史相似案例
     */
    public List<MatchKnowledge> getSimilarHistoricalCases(MatchAnalysis analysis, int limit) {
        String query = buildSimilarityQuery(analysis);
        return knowledgeVectorStore.searchSimilar(query, limit, 0.4);
    }

    /**
     * 构建基础分析提示词（优化版）
     */
    private String buildBasePrompt(MatchAnalysis analysis) {
        // 计算赔率深度分析
        String oddsDeepAnalysis = buildOddsDeepAnalysis(analysis.getHadLists(), analysis.getHhadLists());

        return String.format("""
            # 足球比赛专业分析任务

            ## 比赛基本信息
            - **对阵**: %s vs %s
            - **联赛**: %s
            - **时间**: %s

            ## 赔率数据
            ### 胜平负赔率
            | 主胜 | 平局 | 客胜 |
            |------|------|------|
            | %s | %s | %s |

            ### 让球盘
            - 让球: %s
            - 主胜: %s | 平局: %s | 客胜: %s

            %s

            ## 数据分析依据

            ### 1. 同赔率历史比赛结果（重要参考）
            %s

            ### 2. 近期交锋记录
            %s

            ### 3. 主队近期状态
            %s

            ### 4. 客队近期状态
            %s

            ### 5. 比赛特征数据
            %s

            ### 6. 赔率变化趋势
            %s

            ### 7. 最新情报
            %s

            ## 分析框架（必须严格执行）

            ### 第一步：概率计算
            根据上面的隐含概率数据，评估市场对各结果的预期。

            ### 第二步：数据验证
            用同赔率历史结果验证市场预期是否合理：
            - 统计同赔率比赛中主胜/平局/客胜的实际比例
            - 对比隐含概率与历史实际概率的偏差

            ### 第三步：调整因素
            根据以下因素调整概率：
            - 近期状态（近5场胜率、进球数）
            - 交锋记录（主客场优势）
            - 情报影响（伤停、轮换）

            ### 第四步：价值判断
            - 如果调整后概率 > 隐含概率 + 5%%，该结果有价值
            - 关注凯利值 > 0 的选项

            ### 第五步：最终决策
            选择期望值最高的结果，而非简单的低赔率

            ## 输出格式要求

            1. **比分预测**: 使用 `{比分}` 格式，如 `{2:1}`
            2. **胜负预测**: 使用 `【结果】` 格式，如 `【主胜】`
            3. 预测结果必须是：主胜、平局、客胜 三选一
            4. **置信度**: 高/中/低

            ## 决策红线
            - ❌ 禁止：仅因赔率低就选择该结果
            - ❌ 禁止：忽视同赔率历史数据
            - ✅ 必须：给出选择该结果的数据支撑
            - ✅ 必须：说明主要风险点
            """,
            analysis.getHomeTeam(), analysis.getAwayTeam(),
            analysis.getLeague(), analysis.getMatchTime(),
            getH(analysis.getHadLists()),
            getD(analysis.getHadLists()),
            getA(analysis.getHadLists()),
            getGoalLine(analysis.getHhadLists()),
            getH(analysis.getHhadLists()),
            getD(analysis.getHhadLists()),
            getA(analysis.getHhadLists()),
            oddsDeepAnalysis,
            formatSimilarMatches(JSONObject.toJSONString(analysis.getSimilarMatches())),
            formatRecentMatches(JSONObject.toJSONString(analysis.getRecentMatches())),
            analysis.getMatchHistoryData() != null ? analysis.getMatchHistoryData().getHome() : "暂无数据",
            analysis.getMatchHistoryData() != null ? analysis.getMatchHistoryData().getAway() : "暂无数据",
            analysis.getMatchAnalysisData() != null ? analysis.getMatchAnalysisData().toString() : "暂无数据",
            formatOddsHistory(analysis.getHadLists()),
            analysis.getInformation() != null ? analysis.getInformation() : "暂无情报"
        );
    }

    /**
     * 构建赔率深度分析
     */
    private String buildOddsDeepAnalysis(List<HadList> hadLists, List<HadList> hhadLists) {
        if (CollectionUtils.isEmpty(hadLists)) {
            return "";
        }

        try {
            HadList latest = hadLists.getFirst();
            double h = Double.parseDouble(latest.getH());
            double d = Double.parseDouble(latest.getD());
            double a = Double.parseDouble(latest.getA());

            // 计算隐含概率(去除抽水)
            double totalProb = 1/h + 1/d + 1/a;
            double margin = (totalProb - 1) * 100;

            double homeProb = (1/h) / totalProb * 100;
            double drawProb = (1/d) / totalProb * 100;
            double awayProb = (1/a) / totalProb * 100;

            // 计算凯利值
            double homeKelly = (h * homeProb/100 - 1) / (h - 1);
            double drawKelly = (d * drawProb/100 - 1) / (d - 1);
            double awayKelly = (a * awayProb/100 - 1) / (a - 1);

            // 分析赔率特征
            String pattern = analyzeOddsPattern(h, d, a);

            // 分析赔率变化
            String trend = "";
            if (hadLists.size() >= 2) {
                HadList first = hadLists.getLast();
                double hChange = h - Double.parseDouble(first.getH());
                double dChange = d - Double.parseDouble(first.getD());
                double aChange = a - Double.parseDouble(first.getA());

                StringBuilder trendBuilder = new StringBuilder();
                if (Math.abs(hChange) > 0.05) {
                    trendBuilder.append(hChange > 0 ? "主胜↑(降热) " : "主胜↓(升热) ");
                }
                if (Math.abs(dChange) > 0.05) {
                    trendBuilder.append(dChange > 0 ? "平局↑ " : "平局↓(关注) ");
                }
                if (Math.abs(aChange) > 0.05) {
                    trendBuilder.append(aChange > 0 ? "客胜↑(降热) " : "客胜↓(升热) ");
                }
                trend = trendBuilder.toString().isEmpty() ? "赔率稳定" : trendBuilder.toString();
            }

            return String.format("""

                ### 赔率深度分析（AI必读）

                #### 隐含概率（去除%.1f%%抽水）
                | 结果 | 隐含概率 | 凯利值 | 价值判断 |
                |------|----------|--------|----------|
                | 主胜 | %.1f%% | %.3f | %s |
                | 平局 | %.1f%% | %.3f | %s |
                | 客胜 | %.1f%% | %.3f | %s |

                #### 赔率特征
                %s

                #### 赔率变动
                %s

                #### 关键信号
                %s
                """,
                margin,
                homeProb, homeKelly, homeKelly > 0.05 ? "⭐有价值" : "一般",
                drawProb, drawKelly, drawKelly > 0.05 ? "⭐有价值" : "一般",
                awayProb, awayKelly, awayKelly > 0.05 ? "⭐有价值" : "一般",
                pattern,
                trend,
                generateKeySignals(h, d, a, homeProb, drawProb, awayProb)
            );

        } catch (Exception e) {
            return "";
        }
    }

    private String analyzeOddsPattern(double h, double d, double a) {
        StringBuilder pattern = new StringBuilder();

        if (h < 1.5) {
            pattern.append("【极强主场】主队绝对优势，但冷门价值高；");
        } else if (h < 1.8) {
            pattern.append("【主队优势】主队明显优势盘；");
        } else if (a < 1.5) {
            pattern.append("【极强客场】客队碾压局，警惕冷门；");
        } else if (a < 1.8) {
            pattern.append("【客队优势】客队占优；");
        } else if (Math.abs(h - a) < 0.3) {
            pattern.append("【势均力敌】两队接近，平局概率增加；");
        }

        if (d < 3.0) {
            pattern.append("【平局热门】市场预期闷平可能大；");
        } else if (d > 4.0) {
            pattern.append("【平局冷门】市场认为会分胜负；");
        }

        return pattern.toString();
    }

    private String generateKeySignals(double h, double d, double a,
                                       double homeProb, double drawProb, double awayProb) {
        StringBuilder signals = new StringBuilder();

        // 冷门信号
        if (h < 1.5 && homeProb < 70) {
            signals.append("⚠️ 极低赔率但概率未达70%%，冷门风险；");
        }
        if (a < 1.5 && awayProb < 70) {
            signals.append("⚠️ 客队极低赔率，警惕主场爆冷；");
        }

        // 平局信号
        if (drawProb > 28 && d > 3.3) {
            signals.append("📊 平局概率与赔率背离，关注平局；");
        }

        // 价值信号
        if (h > 2.5 && homeProb > 35) {
            signals.append("💰 主胜可能存在价值；");
        }
        if (a > 2.5 && awayProb > 35) {
            signals.append("💰 客胜可能存在价值；");
        }

        return signals.toString().isEmpty() ? "暂无特殊信号" : signals.toString();
    }

    /**
     * 构建带xG数据的分析提示词（优化版）
     */
    private String buildPromptWithXg(MatchAnalysis analysis) {
        return String.format("""
            # 足球比赛专业分析任务 (xG增强版)

            ## 比赛基本信息
            - **对阵**: %s vs %s
            - **联赛**: %s
            - **时间**: %s

            ## 赔率数据
            ### 胜平负赔率
            | 主胜 | 平局 | 客胜 |
            |------|------|------|
            | %s | %s | %s |

            ### 让球盘
            - 让球: %s
            - 主胜: %s | 平局: %s | 客胜: %s

            ## 数据分析依据

            ### 1. 同赔率历史比赛结果
            %s

            ### 2. 近期交锋记录
            %s

            ### 3. 主队近期状态
            %s

            ### 4. 客队近期状态
            %s

            ### 5. 比赛特征数据
            %s

            ### 6. 赔率变化趋势
            %s

            ### 7. xG数据分析 (重要)

            #### 主队主场xG表现
            %s

            #### 客队客场xG表现
            %s

            **xG指标说明**:
            - xG (Expected Goals): 预期进球数
            - xGA (Expected Goals Against): 预期失球数
            - NPxG: 非点球预期进球
            - PPDA: 每防守动作允许传球数 (越低防守越积极)
            - xPTS: 预期积分

            ### 8. 最新情报
            %s

            ## 分析要求

            请按照以下框架进行专业分析：

            ### 1. 赔率深度解读 (权重20%%)
            - 分析赔率反映的市场预期
            - 识别赔率异常波动

            ### 2. 基本面分析 (权重20%%)
            - 历史交锋心理优势
            - 战术风格匹配度

            ### 3. xG数据分析 (权重30%%) ⭐
            - 主队主场创造机会能力 vs 客队客场防守能力
            - 客队客场创造机会能力 vs 主队主场防守能力
            - xG差值预测胜负走势
            - PPDA对比分析攻防节奏

            ### 4. 状态与趋势分析 (权重15%%)
            - 近期表现趋势
            - 进攻防守效率变化

            ### 5. 综合预测 (权重15%%)
            - 基于xG的进球数预测
            - 比分预测逻辑

            ## 输出格式要求

            1. **比分预测**: 使用 `{比分}` 格式，如 `{2:1}`
            2. **胜负预测**: 使用 `【结果】` 格式，如 `【主胜】`
            3. 预测结果必须是：主胜、平局、客胜 三选一

            ## 重要提示
            - xG数据是核心参考指标，需重点分析
            - 关注xG与实际进球的偏差（运气因素）
            - 考虑冷门发生的可能场景
            """,
            analysis.getHomeTeam(), analysis.getAwayTeam(),
            analysis.getLeague(), analysis.getMatchTime(),
            getH(analysis.getHadLists()),
            getD(analysis.getHadLists()),
            getA(analysis.getHadLists()),
            getGoalLine(analysis.getHhadLists()),
            getH(analysis.getHhadLists()),
            getD(analysis.getHhadLists()),
            getA(analysis.getHhadLists()),
                formatSimilarMatches(JSONObject.toJSONString(analysis.getSimilarMatches())),
                formatRecentMatches(JSONObject.toJSONString(analysis.getRecentMatches())),
            analysis.getMatchHistoryData() != null ? analysis.getMatchHistoryData().getHome() : "暂无数据",
            analysis.getMatchHistoryData() != null ? analysis.getMatchHistoryData().getAway() : "暂无数据",
            analysis.getMatchAnalysisData() != null ? analysis.getMatchAnalysisData().toString() : "暂无数据",
            formatOddsHistory(analysis.getHadLists()),
            formatXgStats(analysis.getHomeTeamStats()),
            formatXgStats(analysis.getAwayTeamStats()),
            analysis.getInformation() != null ? analysis.getInformation() : "暂无情报"
        );
    }

    /**
     * 推断比赛场景
     */
    private String inferScenario(MatchAnalysis analysis) {
        StringBuilder scenario = new StringBuilder();

        try {
            var odds = analysis.getHadLists();
            if (odds != null && !odds.isEmpty()) {
                double h = Double.parseDouble(odds.getFirst().getH());
                double d = Double.parseDouble(odds.getFirst().getD());
                double a = Double.parseDouble(odds.getFirst().getA());

                if (h < 1.5) {
                    scenario.append("强队主场大胜盘 ");
                } else if (a < 1.5) {
                    scenario.append("强队客场反客为主 ");
                } else if (Math.abs(h - a) < 0.3) {
                    scenario.append("势均力敌 ");
                }

                if (d < 3.2) {
                    scenario.append("平局概率高 ");
                }
            }
        } catch (Exception e) {
            // ignore
        }

        return scenario.toString().trim();
    }

    private String buildSimilarityQuery(MatchAnalysis analysis) {
        return String.format("联赛:%s 主队:%s 客队:%s 赔率:%s",
            analysis.getLeague(),
            analysis.getHomeTeam(),
            analysis.getAwayTeam(),
            formatOddsInfo(analysis));
    }

    private String formatOddsInfo(MatchAnalysis analysis) {
        if (analysis.getHadLists() == null || analysis.getHadLists().isEmpty()) {
            return "";
        }
        var odds = analysis.getHadLists().getFirst();
        return String.format("主%s平%s客%s", odds.getH(), odds.getD(), odds.getA());
    }

    private String getH(List<HadList> hadLists) {
        if (!CollectionUtils.isEmpty(hadLists)) {
            return hadLists.getFirst().getH();
        }
        return "-";
    }

    private String getD(List<HadList> hadLists) {
        if (!CollectionUtils.isEmpty(hadLists)) {
            return hadLists.getFirst().getD();
        }
        return "-";
    }

    private String getA(List<HadList> hadLists) {
        if (!CollectionUtils.isEmpty(hadLists)) {
            return hadLists.getFirst().getA();
        }
        return "-";
    }

    private String getGoalLine(List<HadList> hhadLists) {
        if (!CollectionUtils.isEmpty(hhadLists)) {
            return hhadLists.getFirst().getGoalLine();
        }
        return "-";
    }

    private String formatSimilarMatches(String similarMatches) {
        return similarMatches != null && !similarMatches.isEmpty() ? similarMatches : "暂无同赔率比赛数据";
    }

    private String formatRecentMatches(String recentMatches) {
        return recentMatches != null && !recentMatches.isEmpty() ? recentMatches : "暂无近期交锋数据";
    }

    private String formatOddsHistory(List<HadList> hadLists) {
        if (CollectionUtils.isEmpty(hadLists)) {
            return "暂无赔率变化数据";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(hadLists.size(), 5); i++) {
            var odds = hadLists.get(i);
            sb.append(String.format("- 主%s 平%s 客%s\n", odds.getH(), odds.getD(), odds.getA()));
        }
        return sb.toString();
    }

    private String formatXgStats(Object stats) {
        if (stats == null) {
            return "暂无xG数据";
        }
        return stats.toString();
    }
}
