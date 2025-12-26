package cn.xingxing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.xingxing.ai.Assistant;
import cn.xingxing.domain.AiAnalysisResult;
import cn.xingxing.dto.MatchAnalysis;
import cn.xingxing.mapper.AiAnalysisResultMapper;
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
        log.info("prompt:{}", prompt);
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
        return chat;
    }

    private String buildAnalysisPrompt(MatchAnalysis analysis) {
        return String.format("""
                        请对 %s vs %s 这场比赛进行专业分析。 
                        比赛基本信息：
                        - 联赛：%s
                        - 比赛时间：%s    
                        最新赔率数据：
                        主队：%s 平局：%s 客队：%s  
                        同赔率比赛结果
                        %s    
                        近期交锋：
                        %s
                        主队近期状态与质量
                        %s
                        客队近期状态与质量
                        %s
                        比赛战术与数据特征
                        %s
                        赔率变化数据
                        %s
                        主队xG数据
                        %s
                        客队xG数据
                        %s    
                        请从以下维度进行综合分析：
                        1. **赔率分析**：解读当前赔率反映的市场预期和胜负概率分布
                        2. **基本面分析**：基于历史交锋记录分析两队战术风格、心理优势和近期状态
                        3. **多因素分析**：基于近期比赛特征、比赛近况分析
                        4. **xG数据分析** 基于xG数据分析(如果有)
                        4. **进球预期**：结合两队攻防特点预测可能的进球数范围  
                        根据本次比赛数据的特点（例如，是否有突出的xG数据、交锋记录是否久远、赔率变动是否剧烈），动态调整各分析维度的权重，并说明理由。     
                        请给出最多三个场景的比分预测以及“概率”和“关键风险提示”.
                        """,
                analysis.getHomeTeam(), analysis.getAwayTeam(),
                analysis.getLeague(), analysis.getMatchTime(),
                analysis.getOddsHistory().getFirst().getHomeWin(),
                analysis.getOddsHistory().getFirst().getDraw(),
                analysis.getOddsHistory().getFirst().getAwayWin(),
                analysis.getOddsHistory(),
                analysis.getRecentMatches(),
                analysis.getMatchHistoryData().getHome(),
                analysis.getMatchHistoryData().getAway(),
                analysis.getMatchAnalysisData(),
                analysis.getHadLists(),
                analysis.getHomeTeamStats(),
                analysis.getAwayTeamStats()
        );
    }


    public String afterMatchAnalysis() {
        LambdaQueryWrapper<AiAnalysisResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(AiAnalysisResult::getMatchResult, "");
        queryWrapper.isNull(AiAnalysisResult::getAfterMatchAnalysis);
        List<AiAnalysisResult> aiAnalysisResults = aiAnalysisResultMapper.selectList(queryWrapper);
        aiAnalysisResults.forEach(result -> {
            String prompt = buildAfterAnalysisPrompt(result);
            String chat = assistant.chatV2(prompt);
            log.info("afterMatchAnalysis {}", chat);
            result.setAfterMatchAnalysis(chat);
            aiAnalysisResultMapper.updateById(result);
        });
       // aiAnalysisResultMapper.updateById(aiAnalysisResults);
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
                        之前提问方式
                        %s 
                        请从以下维度进行综合分析：
                        1. **提问方式调整**：我之前的提问方式哪些需要优化
                        2. **数据支撑**：赛前的数据支撑需要哪些优化
                        3. **其他**：任何其他因素能让你预测更加准确的因素可以提出来       
                        """,
                aiAnalysisResult.getHomeTeam(), aiAnalysisResult.getAwayTeam(),
                aiAnalysisResult.getMatchTime(),
                aiAnalysisResult.getAiAnalysis(),
                aiAnalysisResult.getMatchResult(),
                getAsk()
        );
    }


    String getAsk(){
        String ask = """
                    请对 %s vs %s 这场比赛进行专业分析。 
                        比赛基本信息：
                        - 联赛：%s
                        - 比赛时间：%s    
                        赔率数据：
                        %s      
                        近期交锋：
                        %s
                        比赛近况
                        %s
                        比赛特征
                        %s    
                        请从以下维度进行综合分析：
                        1. **赔率分析**：解读当前赔率反映的市场预期和胜负概率分布
                        2. **基本面分析**：基于历史交锋记录分析两队战术风格、心理优势和近期状态
                        3. **多因素分析**：基于近期比赛特征、比赛近况分析
                        4. **进球预期**：结合两队攻防特点预测可能的进球数范围  
                        权重比例：赔率0.4 基本面0.3 多因素0.3     
                        请给出最多三个场景最可能的比分预测。
                """;
        return ask;
    }
}