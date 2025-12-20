package com.example.demo.service;

import com.example.demo.dto.FeishuMessage;
import com.example.demo.dto.MatchAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class MessageBuilderService {

    public String buildFeishuMessage(List<MatchAnalysis> analyses, boolean includeAi) {
        FeishuMessage message = FeishuMessage.builder()
                .msgType("interactive")
                .card(buildCard(analyses, includeAi))
                .build();

        return toJson(message);
    }

    private FeishuMessage.Card buildCard(List<MatchAnalysis> analyses, boolean includeAi) {
        List<Object> elements = new ArrayList<>();

        // 添加说明
        elements.add(createTextElement("足球比赛分析报告\n分析时间：" + new Date()));
        elements.add(createTextElement("带AI分析,请勿频繁点击: https://www.xingxing2019.cn/demo/api/match/analyze/ai"));
        elements.add(createDivider());

        // 添加每个比赛分析
        for (MatchAnalysis analysis : analyses) {
            elements.add(createMatchElement(analysis, includeAi));
            elements.add(createDivider());
        }

        return FeishuMessage.Card.builder()
                .elements(elements)
                .build();
    }

    private Map<String, Object> createTextElement(String text) {
        Map<String, Object> element = new HashMap<>();
        element.put("tag", "div");

        Map<String, Object> textContent = new HashMap<>();
        textContent.put("tag", "lark_md");
        textContent.put("content", text);

        element.put("text", textContent);
        return element;
    }

    private Map<String, Object> createDivider() {
        Map<String, Object> element = new HashMap<>();
        element.put("tag", "hr");
        return element;
    }

    private Map<String, Object> createMatchElement(MatchAnalysis analysis, boolean includeAi) {
        StringBuilder content = new StringBuilder();

        content.append("**").append(analysis.getHomeTeam()).append(" vs ").append(analysis.getAwayTeam()).append("**\n");
        content.append("联赛：").append(analysis.getLeague()).append("\n");
        content.append("时间：").append(analysis.getMatchTime()).append("\n\n");

        if (analysis.getRecentMatches() != null && !analysis.getRecentMatches().isEmpty()) {
            content.append("**近期交锋：**\n");
            analysis.getRecentMatches().forEach(match ->
                    content.append("- ").append(match.getMatchDate())
                            .append(" ").append(match.getHomeTeam())
                            .append(" ").append(match.getScore())
                            .append(" ").append(match.getAwayTeam()).append("\n")
            );
            content.append("\n");
        }

        if (analysis.getOddsHistory() != null && !analysis.getOddsHistory().isEmpty()) {
            content.append("**最新赔率：**\n");
            analysis.getOddsHistory().forEach(odds ->
                    content.append(String.format("- 主胜: %.2f  平: %.2f  客胜: %.2f\n",
                            odds.getHomeWin(), odds.getDraw(), odds.getAwayWin()))
            );
            content.append("\n");
        }

        if (includeAi && analysis.getAiAnalysis() != null) {
            content.append("**AI分析：**\n");
            content.append(analysis.getAiAnalysis()).append("\n");
        }

        return createTextElement(content.toString());
    }

    private String toJson(Object obj) {
        try {
            // 这里使用你项目中已有的JSON工具，如FastJSON
            return com.alibaba.fastjson.JSONObject.toJSONString(obj);
        } catch (Exception e) {
            log.error("JSON序列化失败", e);
            return "{}";
        }
    }
}