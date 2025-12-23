package com.example.demo.data;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.config.FootballApiConfig;
import com.example.demo.domain.*;
import com.example.demo.dto.*;
import com.example.demo.dto.url5.Match;
import com.example.demo.dto.url5.MatchInfo5;
import com.example.demo.mapper.*;
import com.example.demo.service.AIService;
import com.example.demo.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-22
 * @Version: 1.0
 */
@Service
@Slf4j
public class DataServiceImpl implements DataService {

    @Autowired
    private FootballApiConfig apiConfig;

    @Autowired
    private MatchInfoMapper matchInfoMapper;

    @Autowired
    private HistoricalMatchMapper historicalMatchMapper;

    @Autowired
    private HadListMapperMapper hadListMapperMapper;

    @Autowired
    private SimilarMatchMapper similarMatchMapper;

    @Autowired
    private AiAnalysisResultMapper aiAnalysisResultMapper;

    @Autowired
    private AIService aiService;

    @Override
    public int loadMatchInfoData() {
        String matchListJson = HttpClientUtil.doGet(
                apiConfig.getMatchListUrl(),
                apiConfig.getHttpConnectTimeout()
        );

        MatchInfoResponse matchInfoResponse = JSONObject.parseObject(matchListJson, MatchInfoResponse.class);
        List<MatchInfo> matchInfoList = matchInfoResponse.getValue().getMatchInfoList();
        List<SubMatchInfo> validMatches = filterValidMatches(matchInfoList);
        matchInfoMapper.insertOrUpdate(validMatches);
        return 0;
    }

    @Override
    public int loadHistoryData(Integer matchId) {
        LambdaQueryWrapper<SubMatchInfo> queryWrapper = new LambdaQueryWrapper<>();
        LocalDate localDate = LocalDate.now();

        queryWrapper.between(SubMatchInfo::getMatchDate, localDate, localDate.plusDays(1));
        List<Integer> list = matchInfoMapper.selectList(queryWrapper).stream().map(SubMatchInfo::getMatchId).toList();
        list.forEach(id -> {
            List<HistoricalMatch> recentMatches = getRecentMatches(String.valueOf(id));
            historicalMatchMapper.insertOrUpdate(recentMatches);
        });
        return 0;
    }

    @Override
    public int loadHadListData(Integer matchId) {
        LambdaQueryWrapper<SubMatchInfo> queryWrapper = new LambdaQueryWrapper<>();
        LocalDate localDate = LocalDate.now();

        queryWrapper.between(SubMatchInfo::getMatchDate, localDate, localDate.plusDays(1));
        List<Integer> list = matchInfoMapper.selectList(queryWrapper).stream().map(SubMatchInfo::getMatchId).toList();
        list.forEach(id -> {
            List<HadList> hadList = getHadList(String.valueOf(id));
            if(!CollectionUtils.isEmpty(hadList)) {
                hadListMapperMapper.insertOrUpdate(hadList);
            }
        });
        return 0;
    }

    @Override
    public int loadSimilarMatch(Integer matchId) {
        LambdaQueryWrapper<SubMatchInfo> queryWrapper = new LambdaQueryWrapper<>();
        LocalDate localDate = LocalDate.now();

        queryWrapper.between(SubMatchInfo::getMatchDate, localDate, localDate.plusDays(1));
        List<Integer> list = matchInfoMapper.selectList(queryWrapper).stream().map(SubMatchInfo::getMatchId).toList();
        LambdaQueryWrapper<HadList> hadQuery = new LambdaQueryWrapper<>();
        hadQuery.in(HadList::getMatchId, list);
        List<HadList> hadLists = hadListMapperMapper.selectList(hadQuery);
        if(!CollectionUtils.isEmpty(hadLists)) {
            hadLists.forEach(hadList -> {
                List<SimilarMatch> similarMatches = getSimilarMatches(hadList.getH(), hadList.getA(), hadList.getD(), hadList.getMatchId());
                similarMatchMapper.insertOrUpdate(similarMatches);
            });
        }
        return 0;
    }

    @Override
    public int loadMatchResult(Integer matchId) {
        LambdaQueryWrapper<AiAnalysisResult> queryWrapper = new LambdaQueryWrapper<>();
        LocalDate localDate = LocalDate.now();

        queryWrapper.between(AiAnalysisResult::getMatchTime, localDate, localDate.plusDays(1));

        List<AiAnalysisResult> aiAnalysisResults = aiAnalysisResultMapper.selectList(queryWrapper);
        List<String> matchIds = aiAnalysisResults.stream().map(AiAnalysisResult::getMatchId).toList();
        List<SubMatchInfo> matchResult = getMatchResult(matchIds);
        if(!CollectionUtils.isEmpty(matchResult)) {
            Map<Integer, String> collect = matchResult.stream().collect(Collectors.toMap(SubMatchInfo::getMatchId, SubMatchInfo::getSectionsNo999));
            aiAnalysisResults.forEach(result -> {
                if(collect.containsKey(Integer.valueOf(result.getMatchId()))) {
                    result.setMatchResult(collect.get(Integer.valueOf(result.getMatchId())));
                }
            });
            aiAnalysisResultMapper.updateById(aiAnalysisResults);
        }
        return 0;
    }

    @Override
    public int afterMatchAnalysis(Integer matchId) {
        aiService.afterMatchAnalysis();
        return 0;
    }

    private List<SubMatchInfo> getMatchResult(List<String> matchIds ){
        List<SubMatchInfo> subMatchInfos = new ArrayList<>();
        String url = apiConfig.getMatchResultUrl();
        String response = HttpClientUtil.doGet(url, apiConfig.getHttpConnectTimeout());
        MatchInfoResponse matchInfoResponse = JSONObject.parseObject(response, MatchInfoResponse.class);
        MatchInfoValue value = matchInfoResponse.getValue();
        List<MatchInfo> matchInfoList = value.getMatchInfoList();
        matchInfoList.forEach(info -> {
            info.getSubMatchList().forEach(subMatchInfo -> {
                if(matchIds.contains(String.valueOf(subMatchInfo.getMatchId()))) {
                    subMatchInfos.add(subMatchInfo);
                }
            });
        });
        return subMatchInfos;
    }
    private List<SimilarMatch> getSimilarMatches(String homeWin, String awayWin, String draw, String matchId) {
        try {
            String url = String.format(apiConfig.getSearchOddsUrl(), homeWin, awayWin, draw);
            String response = HttpClientUtil.doGet(url, apiConfig.getHttpConnectTimeout());

            MatchInfoResponse3 matchInfoResponse3 = JSONObject.parseObject(response, MatchInfoResponse3.class);
            List<MatchItem> matchList = matchInfoResponse3.getValue().getMatchList();

            if (!CollectionUtils.isEmpty(matchList)) {
                return matchList.stream()
                        .map(f->convertMatchItem(f,homeWin,awayWin,draw,matchId))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("获取相似比赛失败", e);
        }
        return Collections.emptyList();
    }



    private SimilarMatch convertMatchItem(MatchItem item, String homeWin, String awayWin, String draw, String matchId) {
        return SimilarMatch.builder()
                .homeTeam(item.getHomeTeamAbbName())
                .matchId(String.valueOf(matchId))
                .h(String.valueOf(homeWin))
                .d(String.valueOf(draw))
                .a(String.valueOf(awayWin))
                .awayTeam(item.getAwayTeamAbbName())
                .score(item.getSectionsNo999())
                .league(item.getLeaguesAbbName())
                .build();
    }


    private List<HadList> getHadList(String matchId) {
        try {
            String url = apiConfig.getFixedBonusUrl() + matchId;
            String response = HttpClientUtil.doGet(url, apiConfig.getHttpConnectTimeout());

            MatchInfoResponse2 matchInfoResponse2 = JSONObject.parseObject(response, MatchInfoResponse2.class);
            List<HadList> hhadList = matchInfoResponse2.getValue().getOddsHistory().getHadList();
            if(!CollectionUtils.isEmpty(hhadList)) {
                hhadList.stream().filter(Objects::nonNull).forEach(hadList1 -> {
                    hadList1.setId(matchId+"-"+hadList1.getH()+"-"+hadList1.getA()+"-"+hadList1.getD());
                    hadList1.setMatchId(matchId);
                });
            }

            return hhadList;
        } catch (Exception e) {
            log.error("获取赔率历史失败: {}", matchId, e);
        }

        return Collections.emptyList();
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
                            .map(f->convertToHistoricalMatch(f,matchId))
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            log.error("获取近期比赛失败: {}", matchId, e);
        }

        return Collections.emptyList();
    }
    private HistoricalMatch convertToHistoricalMatch(Match match,String matchId) {
        return HistoricalMatch.builder()
                .homeTeam(match.getHomeTeamShortName())
                .awayTeam(match.getAwayTeamShortName())
                .matchId(matchId)
                .id(match.getMatchId())
                .score(match.getFullCourtGoal())
                .matchDate(match.getMatchDate())
                .build();
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



}
