package cn.xingxing.service;

import cn.xingxing.domain.*;
import cn.xingxing.notify.NotifyService;
import cn.xingxing.vo.AiAnalysisResultVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cn.xingxing.config.FootballApiConfig;
import cn.xingxing.dto.*;
import cn.xingxing.dto.url5.Match;
import cn.xingxing.dto.url5.MatchInfo5;
import cn.xingxing.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FootballAnalysisService {

    @Autowired
    private FootballApiConfig apiConfig;

    @Autowired
    private AIService aiService;

    @Autowired
    private ExecutorService footballExecutor;

    @Autowired
    private MatchInfoService matchInfoService;

    @Autowired
    private HistoricalMatchService historicalMatchService;
    @Autowired
    private SimilarMatchService similarMatchService;

    @Autowired
    private HadListService hadListService;

    @Autowired
    private AiAnalysisResultService aiAnalysisResultService;

    @Autowired
    private TeamStatsService teamStatsService;

    @Autowired
    private InformationService informationService;

    @Autowired
    private NotifyService notifyService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public List<MatchAnalysis> analyzeMatches(String aiInfo, List<SubMatchInfo> validMatches) {
        log.info("开始分析比赛数据...");
        try {
            List<CompletableFuture<MatchAnalysis>> futures = validMatches.stream()
                    .map(match -> CompletableFuture.supplyAsync(() ->
                            analyzeSingleMatch(match, aiInfo), footballExecutor))
                    .toList();

            // 4. 等待所有分析完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            allFutures.join();

            // 5. 收集结果
            List<MatchAnalysis> results = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.info("比赛分析完成，共分析 {} 场比赛", results.size());
            return results;

        } catch (Exception e) {
            log.error("比赛分析失败", e);
            return Collections.emptyList();
        }
    }


    private MatchAnalysis analyzeSingleMatch(SubMatchInfo match, String aiInfo) {
        try {
            String matchId = String.valueOf(match.getMatchId());
            AiAnalysisResultVo byMatchId = aiAnalysisResultService.findByMatchId(matchId);
            // 构建分析对象
            MatchAnalysis analysis = MatchAnalysis.builder()
                    .homeTeam(match.getHomeTeamAbbName())
                    .awayTeam(match.getAwayTeamAbbName())
                    .matchTime(parseMatchTime(match.getMatchDate(), match.getMatchTime()))
                    .league(match.getLeagueAbbName())
                    .matchId(matchId)
                    .build();
            if(byMatchId != null) {
                analysis.setAiAnalysis(byMatchId.getAiAnalysis());
                return analysis;
            }
            // 获取近期交锋记录
            analysis.setRecentMatches(historicalMatchService.findHistoricalMatch(matchId));

            // 获取赔率历史
            analysis.setOddsHistory(getOddsHistory(matchId));
            //特征数据
            analysis.setMatchAnalysisData(getMatchAnalysisData(matchId));
            //近期比赛
            analysis.setMatchHistoryData(getMatchHistoryData(matchId));
            analysis.setHadLists(hadListService.findHadList(matchId));
            analysis.setHomeTeamStats(teamStatsService.selectByTeamName(match.getHomeTeamAbbName()));
            analysis.setAwayTeamStats(teamStatsService.selectByTeamName(match.getAwayTeamAbbName()));
            Information byId = informationService.getById(matchId);
            if(byId!=null){
                analysis.setInformation(byId.getInfo());
            }
            // AI分析
            if ("ai".equals(aiInfo)) {
                analysis.setAiAnalysis(aiService.analyzeMatch(analysis));
            }

            return analysis;

        } catch (Exception e) {
            log.error("分析单场比赛失败: {} vs {}",
                    match.getHomeTeamAbbName(), match.getAwayTeamAbbName(), e);
            return null;
        }
    }

    private MatchHistoryData getMatchHistoryData(String matchId) {
        String url = String.format(apiConfig.getMatchHistoryUrl(),matchId);
        String response = HttpClientUtil.doGet(url, apiConfig.getHttpConnectTimeout());
        MatchHistoryResponse matchAnalysisResponse = JSONObject.parseObject(response, MatchHistoryResponse.class);
        return matchAnalysisResponse.getValue();

    }

    private MatchAnalysisData getMatchAnalysisData(String matchId) {
        try {
            String url = String.format(apiConfig.getMatchFeatureUrl(),matchId);
            String response = HttpClientUtil.doGet(url, apiConfig.getHttpConnectTimeout());
            MatchAnalysisResponse matchAnalysisResponse = JSONObject.parseObject(response, MatchAnalysisResponse.class);
            return matchAnalysisResponse.getValue();
        } catch (Exception e) {
            log.error("获取比赛特征失败", e);
        }

        return null;
    }

    private LocalDateTime parseMatchTime(String date, String time) {
        try {
            String dateTimeStr = date + " " + time;
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("解析比赛时间失败: {} {}", date, time, e);
            return LocalDateTime.now();
        }
    }

    private List<HistoricalMatch> getRecentMatches(String matchId) {
        try {
            String url = String.format(apiConfig.getResultHistoryUrl(), matchId);
            String response = HttpClientUtil.doGet(url, apiConfig.getHttpConnectTimeout());

            JSONObject json = JSONObject.parseObject(response);
            Object value = json.get("value");

            if (value != null) {
                MatchInfo5 matchInfo5 = JSONObject.parseObject(
                        JSONObject.toJSONString(value),
                        MatchInfo5.class
                );

                if (matchInfo5 != null && !CollectionUtils.isEmpty(matchInfo5.getMatchList())) {
                    return matchInfo5.getMatchList().stream()
                            .map(this::convertToHistoricalMatch)
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            log.error("获取近期比赛失败: {}", matchId, e);
        }

        return Collections.emptyList();
    }

    private HistoricalMatch convertToHistoricalMatch(Match match) {
        return HistoricalMatch.builder()
                .homeTeam(match.getHomeTeamShortName())
                .awayTeam(match.getAwayTeamShortName())
                .score(match.getFullCourtGoal())
                .matchDate(match.getMatchDate())
                .build();
    }

    private List<OddsInfo> getOddsHistory(String matchId) {
        try {

            List<HadList> hhadList = hadListService.findHadList(matchId);

            if (!CollectionUtils.isEmpty(hhadList)) {
                HadList latestOdds = hhadList.getLast();
                // 获取相似比赛
                List<SimilarMatch> similarMatches = similarMatchService.findSimilarMatch(matchId);

                OddsInfo oddsInfo = OddsInfo.builder()
                        .homeWin(Double.valueOf(latestOdds.getH()))
                        .draw(Double.valueOf(latestOdds.getD()))
                        .awayWin(Double.valueOf(latestOdds.getA()))
                        .similarMatches(similarMatches)
                        .build();

                return Collections.singletonList(oddsInfo);
            }
        } catch (Exception e) {
            log.error("获取赔率历史失败: {}", matchId, e);
        }

        return Collections.emptyList();
    }

    private List<HistoricalMatch> getSimilarMatches(double homeWin, double awayWin, double draw) {
        try {
            String url = String.format(apiConfig.getSearchOddsUrl(), homeWin, awayWin, draw);
            String response = HttpClientUtil.doGet(url, apiConfig.getHttpConnectTimeout());

            MatchInfoResponse3 matchInfoResponse3 = JSONObject.parseObject(response, MatchInfoResponse3.class);
            List<MatchItem> matchList = matchInfoResponse3.getValue().getMatchList();

            if (!CollectionUtils.isEmpty(matchList)) {
                return matchList.stream()
                        .map(this::convertMatchItem)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("获取相似比赛失败", e);
        }

        return Collections.emptyList();
    }

    private HistoricalMatch convertMatchItem(MatchItem item) {
        return HistoricalMatch.builder()
                .homeTeam(item.getHomeTeamAbbName())
                .awayTeam(item.getAwayTeamAbbName())
                .score(item.getSectionsNo999())
                .league(item.getLeaguesAbbName())
                .build();
    }

    public void analyzeAndNotify(String aiInfo) {
        List<SubMatchInfo> currentDateMatch = matchInfoService.findCurrentDateMatch();

        List<List<SubMatchInfo>> lists = splitIntoBatches(currentDateMatch, 3);
        lists.forEach(list -> {
            List<MatchAnalysis> analyses = analyzeMatches(aiInfo, list);
            notifyService.sendMsg(analyses);
        });

    }

    private List<List<SubMatchInfo>> splitIntoBatches(List<SubMatchInfo> analyses, int batchSize) {
        List<List<SubMatchInfo>> batches = new ArrayList<>();
        for (int i = 0; i < analyses.size(); i += batchSize) {
            int end = Math.min(analyses.size(), i + batchSize);
            batches.add(new ArrayList<>(analyses.subList(i, end)));
        }

        return batches;
    }






    public MatchAnalysis analysisByMatchId(String matchId) {
        SubMatchInfo byId = matchInfoService.getById(matchId);
        return analyzeSingleMatch(byId, "ai");
    }
}