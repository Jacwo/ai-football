package com.example.demo.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.config.FootballApiConfig;
import com.example.demo.dto.*;
import com.example.demo.dto.url5.Match;
import com.example.demo.dto.url5.MatchInfo5;
import com.example.demo.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private AIServiceNew aiServiceNew;

    @Autowired
    private MessageBuilderService messageBuilder;

    @Autowired
    private ExecutorService footballExecutor;

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

    private List<SubMatchInfo> filterValidMatches(List<MatchInfo> matchInfoList) {
        List<SubMatchInfo> validMatches = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (MatchInfo matchInfo : matchInfoList) {
            try {
                Date matchDate = dateFormat.parse(matchInfo.getMatchDate() + " 23:59:59");
                Date now = new Date();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(now);
                calendar.add(Calendar.DAY_OF_MONTH, 2);
                Date twoDaysLater = calendar.getTime();

                // 只分析未来2天内的比赛
                if (matchDate.before(twoDaysLater)) {
                    validMatches.addAll(matchInfo.getSubMatchList());
                }
            } catch (ParseException e) {
                log.warn("日期解析失败: {}", matchInfo.getMatchDate(), e);
            }
        }

        return validMatches;
    }

    private MatchAnalysis analyzeSingleMatch(SubMatchInfo match, String aiInfo) {
        try {
            String matchId = String.valueOf(match.getMatchId());

            // 构建分析对象
            MatchAnalysis analysis = MatchAnalysis.builder()
                    .homeTeam(match.getHomeTeamAbbName())
                    .awayTeam(match.getAwayTeamAbbName())
                    .matchTime(parseMatchTime(match.getMatchDate(), match.getMatchTime()))
                    .league(match.getLeagueAbbName())
                    .build();

            // 获取近期交锋记录
            analysis.setRecentMatches(getRecentMatches(matchId));

            // 获取赔率历史
            analysis.setOddsHistory(getOddsHistory(matchId));

            // AI分析
            if ("ai".equals(aiInfo)) {
                analysis.setAiAnalysis(aiServiceNew.analyzeMatch(analysis));
            }

            return analysis;

        } catch (Exception e) {
            log.error("分析单场比赛失败: {} vs {}",
                    match.getHomeTeamAbbName(), match.getAwayTeamAbbName(), e);
            return null;
        }
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
            String url = apiConfig.getFixedBonusUrl() + matchId;
            String response = HttpClientUtil.doGet(url, apiConfig.getHttpConnectTimeout());

            MatchInfoResponse2 matchInfoResponse2 = JSONObject.parseObject(response, MatchInfoResponse2.class);
            List<HadList> hhadList = matchInfoResponse2.getValue().getOddsHistory().getHadList();

            if (!CollectionUtils.isEmpty(hhadList)) {
                HadList latestOdds = hhadList.get(hhadList.size() - 1);

                // 获取相似比赛
                List<HistoricalMatch> similarMatches = getSimilarMatches(
                        Double.valueOf(latestOdds.getH()), Double.valueOf(latestOdds.getA()), Double.valueOf(latestOdds.getD())
                );

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

    public void analyzeAndNotify(String aiInfo) throws IOException {
        String matchListJson = HttpClientUtil.doGet(
                apiConfig.getMatchListUrl(),
                apiConfig.getHttpConnectTimeout()
        );

        MatchInfoResponse matchInfoResponse = JSONObject.parseObject(matchListJson, MatchInfoResponse.class);
        List<MatchInfo> matchInfoList = matchInfoResponse.getValue().getMatchInfoList();
        List<SubMatchInfo> validMatches = filterValidMatches(matchInfoList);
        List<List<SubMatchInfo>> lists = splitIntoBatches2(validMatches, 3);
        lists.forEach(list -> {
            List<MatchAnalysis> analyses = analyzeMatches(aiInfo, list);

            if (!CollectionUtils.isEmpty(analyses)) {
                // 按每5条一组分批发送
                List<List<MatchAnalysis>> batches = splitIntoBatches(analyses, 3);

                log.info("共分析 {} 场比赛，将分为 {} 批发送",
                        analyses.size(), batches.size());

                // 发送每个批次
                for (int i = 0; i < batches.size(); i++) {
                    List<MatchAnalysis> batch = batches.get(i);
                    log.info("正在发送第 {} 批，包含 {} 场比赛", i + 1, batch.size());

                    // 构建消息
                    String message = messageBuilder.buildFeishuMessage(batch, "ai".equals(aiInfo));

                    try {
                        // 如果是最后一批，可以添加结束标记
                        if (i == batches.size() - 1) {
                            message = addEndMarker(message);
                        }

                        String response = HttpClientUtil.doPost(
                                apiConfig.getFeishuWebhookUrl(),
                                message,
                                apiConfig.getHttpReadTimeout()
                        );
                        log.info("第 {} 批飞书消息发送成功: {}", i + 1, response);

                        // 批次之间添加延迟，避免发送过快
                        if (i < batches.size() - 1) {
                            Thread.sleep(1000); // 1秒延迟
                        }

                    } catch (Exception e) {
                        log.error("第 {} 批飞书消息发送失败", i + 1, e);
                    }
                }
            } else {
                log.info("没有需要分析的比赛");
            }
        });

    }

    private List<List<SubMatchInfo>> splitIntoBatches2(List<SubMatchInfo> analyses, int batchSize) {
        List<List<SubMatchInfo>> batches = new ArrayList<>();

        for (int i = 0; i < analyses.size(); i += batchSize) {
            int end = Math.min(analyses.size(), i + batchSize);
            batches.add(new ArrayList<>(analyses.subList(i, end)));
        }

        return batches;
    }

    private List<List<MatchAnalysis>> splitIntoBatches(List<MatchAnalysis> analyses, int batchSize) {
        List<List<MatchAnalysis>> batches = new ArrayList<>();

        for (int i = 0; i < analyses.size(); i += batchSize) {
            int end = Math.min(analyses.size(), i + batchSize);
            batches.add(new ArrayList<>(analyses.subList(i, end)));
        }

        return batches;
    }



    /**
     * 为最后一批消息添加结束标记
     */
    private String addEndMarker(String message) {
        try {
            // 解析JSON消息
            JSONObject messageJson = JSONObject.parseObject(message);
            JSONObject card = messageJson.getJSONObject("card");
            JSONArray elements = card.getJSONArray("elements");

            // 添加结束标记
            JSONObject endMarker = new JSONObject();
            endMarker.put("tag", "div");

            JSONObject textContent = new JSONObject();
            textContent.put("tag", "lark_md");
            textContent.put("content", "--- 所有比赛分析发送完毕 ---");

            endMarker.put("text", textContent);
            elements.add(endMarker);

            // 重新构建消息
            return messageJson.toJSONString();
        } catch (Exception e) {
            log.warn("添加结束标记失败，返回原始消息", e);
            return message;
        }
    }
}