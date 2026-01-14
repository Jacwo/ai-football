package cn.xingxing.service;

import cn.xingxing.ai.StreamingAssistant;
import cn.xingxing.entity.HadList;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.xingxing.ai.Assistant;
import cn.xingxing.entity.AiAnalysisResult;
import cn.xingxing.dto.MatchAnalysis;
import cn.xingxing.mapper.AiAnalysisResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Slf4j
public class AIService {

    @Autowired
    private Assistant assistant;

    @Autowired
    private StreamingAssistant streamingAssistant;

    @Autowired
    private AiAnalysisResultMapper aiAnalysisResultMapper;

    Pattern win = Pattern.compile("【([^】]+)】");
    Pattern score = Pattern.compile("\\{([^}]+)\\}");
    public static List<String> league = List.of("意甲", "英超", "西甲", "德甲", "法甲");


    public String analyzeMatch(MatchAnalysis analysis) {
        String prompt;
        if (league.contains(analysis.getLeague())) {
            prompt = buildAnalysisPromptWithXg(analysis);
        } else {
            prompt = buildAnalysisPrompt(analysis);
        }
        log.info("prompt:{}", prompt);
        String chat = assistant.chat(prompt);
        AiAnalysisResult aiAnalysisResult = new AiAnalysisResult();
        aiAnalysisResult.setMatchId(analysis.getMatchId());
        aiAnalysisResult.setAwayTeam(analysis.getAwayTeam());
        aiAnalysisResult.setHomeTeam(analysis.getHomeTeam());
        aiAnalysisResult.setMatchTime(analysis.getMatchTime());
        if (!CollectionUtils.isEmpty(analysis.getHadLists())) {
            aiAnalysisResult.setDraw(String.valueOf(analysis.getHadLists().getFirst().getD()));
            aiAnalysisResult.setAwayWin(String.valueOf(analysis.getHadLists().getFirst().getA()));
            aiAnalysisResult.setHomeWin(String.valueOf(analysis.getHadLists().getFirst().getH()));
        }
        aiAnalysisResult.setAiAnalysis(chat);
        System.out.println(chat);
        Matcher matcher = score.matcher(chat);
        if (matcher.find()) {
            System.out.println("ai比分---" + matcher.group(1));
            aiAnalysisResult.setAiScore(matcher.group(1).replace("{", "").replace("}", ""));
        }
        Matcher matcher2 = win.matcher(chat);
        if (matcher2.find()) {
            System.out.println("ai胜负---" + matcher2.group(1));
            aiAnalysisResult.setAiResult(matcher2.group(1).replace("【", "").replace("】", ""));

        }
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
                        让球赔率数据
                        让 %s 主队：%s 平局：%s 客队：%s  
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
                        最新情报
                        %s    
                        请从以下维度进行综合分析：
                        1. **赔率分析**：解读赔率反映的市场预期和胜负概率分布
                        2. **基本面分析**：基于历史交锋记录分析两队战术风格、心理优势和近期状态
                        3. **多因素分析**：基于近期比赛特征、比赛近况分析
                        4. **进球预期**：结合两队攻防特点预测可能的进球数范围  
                        根据本次比赛数据的特点（例如，是否有突出的xG数据、交锋记录是否久远、赔率变动是否剧烈），动态调整各分析维度的权重，
                        你不要谁的赔率低你就猜测谁赢，考虑冷门场景，并说明理由。     
                        请给出1个比分推荐考虑冷门场景，以及胜平负推荐使用(主胜、平局、客胜表示)，比分结果使用{}修饰，胜负推荐使用【】修饰”.
                        """,
                analysis.getHomeTeam(), analysis.getAwayTeam(),
                analysis.getLeague(), analysis.getMatchTime(),
                getH(analysis.getHadLists()),
                getD(analysis.getHadLists()),
                getA(analysis.getHadLists()),
                getGoline(analysis.getHhadLists()),
                getH(analysis.getHhadLists()),
                getD(analysis.getHhadLists()),
                getA(analysis.getHhadLists()),
                analysis.getSimilarMatches(),
                analysis.getRecentMatches(),
                analysis.getMatchHistoryData().getHome(),
                analysis.getMatchHistoryData().getAway(),
                analysis.getMatchAnalysisData(),
                analysis.getHadLists(),
                analysis.getInformation()
        );
    }

    private String getGoline(List<HadList> hhadLists) {
        if(!CollectionUtils.isEmpty(hhadLists)){
            return hhadLists.getFirst().getGoalLine();
        }else
            return "";
    }


    private String buildAnalysisPromptWithXg(MatchAnalysis analysis) {
        return String.format("""
                        请对 %s vs %s 这场比赛进行专业分析。 
                        比赛基本信息：
                        - 联赛：%s
                        - 比赛时间：%s    
                        最新赔率数据：
                        主队：%s 平局：%s 客队：%s  
                        让球赔率数据
                        让 %s 主队：%s 平局：%s 客队：%s  
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
                        主队主场xG数据
                        %s
                        客队客场xG数据
                        %s
                        最新情报
                        %s    
                        请从以下维度进行综合分析：
                        1. **赔率分析**：解读赔率反映的市场预期和胜负概率分布
                        2. **基本面分析**：基于历史交锋记录分析两队战术风格、心理优势和近期状态
                        3. **多因素分析**：基于近期比赛特征、比赛近况分析
                        4. **xG数据分析** 基于xG数据分析(如果有)
                        4. **进球预期**：结合两队攻防特点预测可能的进球数范围  
                        根据本次比赛数据的特点（例如，是否有突出的xG数据、交锋记录是否久远、赔率变动是否剧烈），动态调整各分析维度的权重，
                        你不要谁的赔率低你就猜测谁赢，考虑冷门场景，并说明理由。     
                        请给出1个比分推荐考虑冷门场景，以及胜平负推荐使用(主胜、平局、客胜表示)，比分结果使用{}修饰，胜负推荐使用【】修饰”.
                        """,
                analysis.getHomeTeam(), analysis.getAwayTeam(),
                analysis.getLeague(), analysis.getMatchTime(),
                getH(analysis.getHadLists()),
                getD(analysis.getHadLists()),
                getA(analysis.getHadLists()),
                getGoline(analysis.getHhadLists()),
                getH(analysis.getHhadLists()),
                getD(analysis.getHhadLists()),
                getA(analysis.getHhadLists()),
                analysis.getSimilarMatches(),
                analysis.getRecentMatches(),
                analysis.getMatchHistoryData().getHome(),
                analysis.getMatchHistoryData().getAway(),
                analysis.getMatchAnalysisData(),
                analysis.getHadLists(),
                analysis.getHomeTeamStats(),
                analysis.getAwayTeamStats(),
                analysis.getInformation()
        );
    }

    private String getA(List<HadList> hadLists) {
        if(!CollectionUtils.isEmpty(hadLists)) {
            return hadLists.getFirst().getA();
        }else{
            return "";
        }
    }

    private String getD(List<HadList> hadLists) {
        if(!CollectionUtils.isEmpty(hadLists)) {
            return hadLists.getFirst().getD();
        }else{
            return "";
        }
    }

    private String getH(List<HadList> hadLists) {
        if(!CollectionUtils.isEmpty(hadLists)) {
            return hadLists.getFirst().getH();
        }else{
            return "";
        }
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


    String getAsk() {
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

    public Flux<String> analyzeMatchStream(MatchAnalysis analysis) {
        String s = this.buildAnalysisPrompt(analysis);
        return streamingAssistant.chat(s);
    }
}