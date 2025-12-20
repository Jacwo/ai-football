package com.example.demo.service;

import com.example.demo.ai.Assistant;
import com.example.demo.dto.MatchAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class AIServiceNew {

    @Autowired
    private Assistant assistant;

    public String analyzeMatch(MatchAnalysis analysis) {
        String prompt = buildAnalysisPrompt(analysis);
        String chat = assistant.chat(prompt);
        log.info("比赛分析结果： {}", chat);
        // 使用Assistant接口（如果有的话）或直接调用API
        return chat;
    }

    private String buildAnalysisPrompt(MatchAnalysis analysis) {
        return String.format("""
            请对 %s vs %s 这场比赛进行专业分析。 
            比赛基本信息：
            - 联赛：%s
            - 比赛时间：%s    
            赔率数据：
            %s      
            近期交锋：
            %s    
            请从以下维度进行综合分析：
            1. **赔率分析**：解读当前赔率反映的市场预期和胜负概率分布
            2. **基本面分析**：基于历史交锋记录分析两队战术风格、心理优势和近期状态
            3. **进球预期**：结合两队攻防特点预测可能的进球数范围       
            请给出2个最可能的比分预测。
            """,
                analysis.getHomeTeam(), analysis.getAwayTeam(),
                analysis.getLeague(), analysis.getMatchTime(),
                analysis.getOddsHistory(),
                analysis.getRecentMatches()
        );
    }
}