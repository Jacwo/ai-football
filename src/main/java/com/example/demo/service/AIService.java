package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.ai.Assistant;
import com.example.demo.domain.AiAnalysisResult;
import com.example.demo.dto.AfterMatchAnalysis;
import com.example.demo.dto.MatchAnalysis;
import com.example.demo.mapper.AiAnalysisResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Service
@Slf4j
public class AIService {

    @Autowired
    private Assistant assistant;

    @Autowired
    private AiAnalysisResultMapper aiAnalysisResultMapper;

    public String analyzeMatch(MatchAnalysis analysis) {
        String prompt = buildAnalysisPrompt(analysis);
        String chat = assistant.chat(prompt);
        AiAnalysisResult aiAnalysisResult = new AiAnalysisResult();
        aiAnalysisResult.setMatchId(analysis.getMatchId());
        aiAnalysisResult.setAwayTeam(analysis.getAwayTeam());
        aiAnalysisResult.setHomeTeam(analysis.getHomeTeam());
        aiAnalysisResult.setMatchTime(analysis.getMatchTime());
        if (!CollectionUtils.isEmpty(analysis.getOddsHistory())) {
            aiAnalysisResult.setDraw(String.valueOf(analysis.getOddsHistory().getFirst().getDraw()));
            aiAnalysisResult.setAwayWin(String.valueOf(analysis.getOddsHistory().getFirst().getAwayWin()));
            aiAnalysisResult.setHomeWin(String.valueOf(analysis.getOddsHistory().getFirst().getHomeWin()));
        }
        aiAnalysisResult.setAiAnalysis(chat);
        aiAnalysisResultMapper.insert(aiAnalysisResult);
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


    public String afterMatchAnalysis() {
        LambdaQueryWrapper<AiAnalysisResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(AiAnalysisResult::getMatchResult, "");
        List<AiAnalysisResult> aiAnalysisResults = aiAnalysisResultMapper.selectList(queryWrapper);
        aiAnalysisResults.forEach(result -> {
            String prompt = buildAfterAnalysisPrompt(result);
            String chat = assistant.chatV2(prompt);
            log.info("afterMatchAnalysis {}", chat);
            result.setAfterMatchAnalysis(chat);
        });
        aiAnalysisResultMapper.updateById(aiAnalysisResults);
        return "";
    }


    private String buildAfterAnalysisPrompt(AiAnalysisResult aiAnalysisResult) {
        return String.format("""
                        请对 %s vs %s 这场比赛进行复盘分析。 
                        比赛基本信息：
                        - 比赛时间：%s    
                        赛前分析内容：
                        %s      
                        最终结果：
                        %s    
                        请从以下维度进行综合分析：
                        1. **提问方式调整**：我之前的提问方式哪些需要优化
                        2. **数据支撑**：赛前的数据支撑需要哪些优化
                        3. **其他**：任何其他因素能让你预测更加准确的因素可以提出来       
                        """,
                aiAnalysisResult.getHomeTeam(), aiAnalysisResult.getAwayTeam(),
                aiAnalysisResult.getMatchTime(),
                aiAnalysisResult.getAiAnalysis(),
                aiAnalysisResult.getMatchResult()
        );
    }
}