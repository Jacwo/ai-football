package com.example.demo.service;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.config.FootballApiConfig;
import com.example.demo.dto.MatchAnalysis;
import com.example.demo.dto.OddsInfo;
import com.example.demo.dto.HistoricalMatch;
import com.example.demo.dto.SimilarMatch;
import com.example.demo.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.demo.util.HttpClientUtil.METHOD_POST;

@Slf4j
@Service
public class AIService {

    @Autowired
    private FootballApiConfig apiConfig;

    private static final String SYSTEM_PROMPT = """
        你是一位专业的足球比赛分析师，擅长基于赔率数据、历史交锋记录进行综合分析。
        请用专业、客观的态度进行分析，给出有理有据的比分预测。
        请从以下维度进行综合分析：
        1. **赔率分析**：解读当前赔率反映的市场预期和胜负概率分布
        2. **基本面分析**：基于历史交锋记录分析两队战术风格、心理优势和近期状态
        3. **进球预期**：结合两队攻防特点预测可能的进球数范围
        请给出2个最可能的比分预测。
        """;

    public String analyzeMatch(MatchAnalysis analysis) {
        String prompt = buildAnalysisPrompt(analysis);

        Map<String, Object> data = new HashMap<>();
        data.put("model", "deepseek-chat");
        data.put("stream", false);

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", SYSTEM_PROMPT);
        messages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);

        data.put("messages", messages);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + apiConfig.getDeepseekApiKey());

        try {
            String response = HttpClientUtil.getHttpContent(
                    apiConfig.getDeepseekApiUrl(),
                    METHOD_POST,
                    JSONObject.toJSONString(data),
                    headers,
                    apiConfig.getHttpReadTimeout()
            );

            JSONObject jsonResponse = JSONObject.parseObject(response);
            return jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        } catch (Exception e) {
            log.error("AI分析失败: {}", e.getMessage(), e);
            return "AI分析暂时不可用，请稍后重试";
        }
    }

    private String buildAnalysisPrompt(MatchAnalysis analysis) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format("请对 %s vs %s 这场比赛进行专业分析。\n\n",
                analysis.getHomeTeam(), analysis.getAwayTeam()));

        prompt.append("比赛基本信息：\n");
        prompt.append(String.format("- 联赛：%s\n", analysis.getLeague()));
        prompt.append(String.format("- 比赛时间：%s\n\n", analysis.getMatchTime()));

        if (analysis.getOddsHistory() != null && !analysis.getOddsHistory().isEmpty()) {
            prompt.append("赔率数据：\n");
            OddsInfo latestOdds = analysis.getOddsHistory().get(0);
            prompt.append(String.format("- 主胜：%.2f，平：%.2f，客胜：%.2f\n",
                    latestOdds.getHomeWin(), latestOdds.getDraw(), latestOdds.getAwayWin()));

            if (latestOdds.getSimilarMatches() != null && !latestOdds.getSimilarMatches().isEmpty()) {
                prompt.append("\n相似历史比赛：\n");
                for (SimilarMatch match : latestOdds.getSimilarMatches()) {
                    prompt.append(String.format("- %s vs %s：比分 %s（联赛：%s）\n",
                            match.getHomeTeam(), match.getAwayTeam(),
                            match.getScore(), match.getLeague()));
                }
            }
            prompt.append("\n");
        }

        if (analysis.getRecentMatches() != null && !analysis.getRecentMatches().isEmpty()) {
            prompt.append("近期交锋记录：\n");
            for (HistoricalMatch match : analysis.getRecentMatches()) {
                prompt.append(String.format("- %s：%s vs %s 比分 %s\n",
                        match.getMatchDate(), match.getHomeTeam(),
                        match.getAwayTeam(), match.getScore()));
            }
        }

        return prompt.toString();
    }
}