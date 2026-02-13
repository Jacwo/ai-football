package cn.xingxing.data;


import cn.xingxing.common.config.FootballApiConfig;
import cn.xingxing.dto.*;
import cn.xingxing.dto.url5.Match;
import cn.xingxing.dto.url5.MatchInfo5;
import cn.xingxing.entity.*;
import cn.xingxing.mapper.*;
import cn.xingxing.common.util.HttpClientUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    private TeamStatsService teamStatsService;


    @Override
    public int syncMatchInfoData() {
        String matchListJson = HttpClientUtil.doGet(
                apiConfig.getMatchListUrl(),
                apiConfig.getHttpConnectTimeout()
        );
        MatchInfoResponse matchInfoResponse = JSONObject.parseObject(matchListJson, MatchInfoResponse.class);
        List<MatchInfo> matchInfoList = matchInfoResponse.getValue().getMatchInfoList();
        matchInfoMapper.insertOrUpdate(matchInfoList.stream().flatMap(list -> list.getSubMatchList().stream()).toList());
        this.syncHadListData();
        return 0;
    }

    @Override
    public int syncHistoryData() {
        List<Integer> list = new ArrayList<>(matchInfoMapper.selectList(buildMatchInfoQuery()).stream().map(SubMatchInfo::getMatchId).toList());
        list.forEach(id -> {
            List<HistoricalMatch> recentMatches = getRecentMatches(String.valueOf(id));
            historicalMatchMapper.insertOrUpdate(recentMatches);
        });
        return 0;
    }


    @Override
    public int loadHistoryDataByMatchId(String matchId) {
        List<HistoricalMatch> recentMatches = getRecentMatches(matchId);
        historicalMatchMapper.insertOrUpdate(recentMatches);
        return 0;
    }

    @Override
    public int syncHadListData() {
        List<SubMatchInfo> subMatchInfos = matchInfoMapper.selectList(buildMatchInfoQuery());
        subMatchInfos.forEach(matchInfo -> {
            int matchId = matchInfo.getMatchId();
            OddsHistory oddsHistory = getOddsInfo(String.valueOf(matchId));
            if (oddsHistory != null) {
                List<HadList> hadList = oddsHistory.getHadList();
                if (!CollectionUtils.isEmpty(hadList)) {
                    hadList.stream().filter(Objects::nonNull).forEach(hList -> {
                        hList.setId((matchId + hList.getUpdateDate() + hList.getUpdateTime()).replace("-", ""));
                        hList.setMatchId(String.valueOf(matchId));
                    });
                    hadListMapperMapper.insertOrUpdate(hadList);
                    HadList last = hadList.getLast();
                    matchInfo.setHomeWin(last.getH());
                    matchInfo.setAwayWin(last.getA());
                    matchInfo.setDraw(last.getD());
                    matchInfo.setIsSingleMatch(isSingleMatch(oddsHistory.getSingleList()));
                    matchInfoMapper.updateById(matchInfo);
                }

                List<HadList> hhadList = oddsHistory.getHhadList();
                if (!CollectionUtils.isEmpty(hhadList)) {
                    hhadList.stream().filter(Objects::nonNull).forEach(hhList -> {
                        hhList.setId((matchId + hhList.getUpdateDate() + hhList.getUpdateTime() + hhList.getGoalLine()).replace("-", ""));
                        hhList.setMatchId(String.valueOf(matchId));
                    });
                    hadListMapperMapper.insertOrUpdate(hhadList);

                    HadList last = hhadList.getLast();
                    matchInfo.setHhomeWin(last.getH());
                    matchInfo.setHawayWin(last.getA());
                    matchInfo.setHdraw(last.getD());
                    matchInfo.setGoalLine(last.getGoalLine());
                    matchInfoMapper.updateById(matchInfo);
                }
            }


        });
        return 0;
    }

    private Boolean isSingleMatch(List<SingleList> singleList) {
        return !singleList.stream().filter(list -> list.getPoolCode().equals("HAD") && 1 == list.getSingle()).findFirst().isEmpty();
    }

    @Override
    public int syncSimilarMatch() {
        LambdaQueryWrapper<SubMatchInfo> queryWrapper = new LambdaQueryWrapper<>();
        LocalDate localDate = LocalDate.now();
        queryWrapper.between(SubMatchInfo::getMatchDate, localDate, localDate.plusDays(1));
        List<Integer> list = matchInfoMapper.selectList(queryWrapper).stream().map(SubMatchInfo::getMatchId).toList();
        LambdaQueryWrapper<HadList> hadQuery = new LambdaQueryWrapper<>();
        hadQuery.in(HadList::getMatchId, list);
        hadQuery.eq(HadList::getGoalLine, "");
        List<HadList> hadLists = hadListMapperMapper.selectList(hadQuery);
        if (!CollectionUtils.isEmpty(hadLists)) {
            hadLists.forEach(hadList -> {
                List<SimilarMatch> similarMatches = getSimilarMatches(hadList.getH(), hadList.getA(), hadList.getD(), hadList.getMatchId());
                similarMatchMapper.insertOrUpdate(similarMatches);
            });
        }
        return 0;
    }


    @Override
    public int loadSimilarMatchByMatchId(String matchId) {
        LambdaQueryWrapper<HadList> hadQuery = new LambdaQueryWrapper<>();
        hadQuery.eq(HadList::getMatchId, matchId);
        hadQuery.eq(HadList::getGoalLine,"");
        List<HadList> hadLists = hadListMapperMapper.selectList(hadQuery);
        if (!CollectionUtils.isEmpty(hadLists)) {
            hadLists.forEach(hadList -> {
                List<SimilarMatch> similarMatches = getSimilarMatches(hadList.getH(), hadList.getA(), hadList.getD(), hadList.getMatchId());
                similarMatchMapper.insertOrUpdate(similarMatches);
            });
        }
        return 0;
    }


    @Override
    public int syncMatchResult() {
        LambdaQueryWrapper<AiAnalysisResult> queryWrapper = new LambdaQueryWrapper<>();
        LocalDate localDate = LocalDate.now();
        queryWrapper.between(AiAnalysisResult::getMatchTime, localDate.minusDays(5), localDate.plusDays(1));
        List<AiAnalysisResult> aiAnalysisResults = aiAnalysisResultMapper.selectList(queryWrapper);
        List<SubMatchInfo> matchResult = getMatchResult();
        if (!CollectionUtils.isEmpty(matchResult)) {
            Map<Integer, String> collect = matchResult.stream().collect(Collectors.toMap(SubMatchInfo::getMatchId, SubMatchInfo::getSectionsNo999));
            aiAnalysisResults.forEach(result -> {
                if (collect.containsKey(Integer.valueOf(result.getMatchId()))) {
                    result.setMatchResult(collect.get(Integer.valueOf(result.getMatchId())));
                }
            });
            aiAnalysisResultMapper.updateById(aiAnalysisResults);
            List<Integer> list = matchResult.stream().map(SubMatchInfo::getMatchId).toList();
            LambdaUpdateWrapper<SubMatchInfo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(SubMatchInfo::getMatchId, list);
            updateWrapper.set(SubMatchInfo::getMatchStatus, "3");
            updateWrapper.set(SubMatchInfo::getMatchStatusName, "已完成");
            matchInfoMapper.update(updateWrapper);
        }
        return 0;
    }

    @Override
    public int afterMatchAnalysis() {

        return 0;
    }

    @Override
    public int loadTeamStats() {

        List<String> list = new ArrayList<>();
        list.add("EPL");//英超
        list.add("La_liga");//西甲
        list.add("Ligue_1");//法甲
        list.add("Bundesliga");//德甲
        list.add("Serie_A");//意甲
        list.stream().forEach(f ->
                teamStatsService.loadTeamStats(f));

        return 0;
    }


    @Override
    public int loadTeamStatsHome() {

        List<String> list = new ArrayList<>();
        list.add("EPL_home");//英超
        list.add("La_liga_home");//西甲
        list.add("Ligue_1_home");//法甲
        list.add("Bundesliga_home");//德甲
        list.add("Serie_A_home");//意甲
        list.stream().forEach(f ->
                teamStatsService.loadTeamStatsHome(f));

        return 0;
    }

    @Override
    public int loadTeamStatsAway() {

        List<String> list = new ArrayList<>();
        list.add("EPL_away");//英超
        list.add("La_liga_away");//西甲
        list.add("Ligue_1_away");//法甲
        list.add("Bundesliga_away");//德甲
        list.add("Serie_A_away");//意甲
        list.stream().forEach(f ->
                teamStatsService.loadTeamStatsAway(f));

        return 0;
    }

    @Override
    public void syncHadListByMatchId(String matchId) {
        OddsHistory oddsInfo = getOddsInfo(matchId);
        List<HadList> hadList = oddsInfo.getHadList();
        if (!CollectionUtils.isEmpty(hadList)) {
            hadList.stream().filter(Objects::nonNull).forEach(hList -> {
                hList.setId((matchId + hList.getUpdateDate() + hList.getUpdateTime()).replace("-", ""));
                hList.setMatchId(String.valueOf(matchId));
            });
            hadListMapperMapper.insertOrUpdate(hadList);
        }
        List<HadList> hhadList = oddsInfo.getHhadList();
        if (!CollectionUtils.isEmpty(hhadList)) {
            hhadList.stream().filter(Objects::nonNull).forEach(hhList -> {
                hhList.setId((matchId + hhList.getUpdateDate() + hhList.getUpdateTime() + hhList.getGoalLine()).replace("-", ""));
                hhList.setMatchId(String.valueOf(matchId));
            });
            hadListMapperMapper.insertOrUpdate(hhadList);
        }
    }

    private List<SubMatchInfo> getMatchResult() {
        String url = apiConfig.getMatchResultUrl();
        String response = HttpClientUtil.doGet(url, apiConfig.getHttpConnectTimeout());
        MatchInfoResponse matchInfoResponse = JSONObject.parseObject(response, MatchInfoResponse.class);
        MatchInfoValue value = matchInfoResponse.getValue();
        List<MatchInfo> matchInfoList = value.getMatchInfoList();
        return matchInfoList.stream().flatMap((MatchInfo matchInfo) -> matchInfo.getSubMatchList().stream()).collect(Collectors.toList());
    }

    private List<SimilarMatch> getSimilarMatches(String homeWin, String awayWin, String draw, String matchId) {
        try {
            String url = String.format(apiConfig.getSearchOddsUrl(), homeWin, awayWin, draw);
            String response = HttpClientUtil.doGet(url, apiConfig.getHttpConnectTimeout());

            MatchInfoResponse3 matchInfoResponse3 = JSONObject.parseObject(response, MatchInfoResponse3.class);
            List<MatchItem> matchList = matchInfoResponse3.getValue().getMatchList();

            if (!CollectionUtils.isEmpty(matchList)) {
                return matchList.stream()
                        .map(f -> convertMatchItem(f, homeWin, awayWin, draw, matchId))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("获取相似比赛失败", e);
        }
        return Collections.emptyList();
    }


    private SimilarMatch convertMatchItem(MatchItem item, String homeWin, String awayWin, String draw, String matchId) {
        return SimilarMatch.builder()
                .id(matchId + "-" + homeWin + "-" + draw + "-" + awayWin)
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


    private OddsHistory getOddsInfo(String matchId) {
        try {
            String url = apiConfig.getFixedBonusUrl() + matchId;
            String response = HttpClientUtil.doGet(url, apiConfig.getHttpConnectTimeout());

            MatchInfoResponse2 matchInfoResponse2 = JSONObject.parseObject(response, MatchInfoResponse2.class);
            return matchInfoResponse2.getValue().getOddsHistory();
        } catch (Exception e) {
            log.error("获取赔率历史失败: {}", matchId, e);
        }
        return null;
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
                            .map(f -> convertToHistoricalMatch(f, matchId))
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            log.error("获取近期比赛失败: {}", matchId, e);
        }

        return Collections.emptyList();
    }

    private HistoricalMatch convertToHistoricalMatch(Match match, String matchId) {
        return HistoricalMatch.builder()
                .homeTeam(match.getHomeTeamShortName())
                .awayTeam(match.getAwayTeamShortName())
                .matchId(matchId)
                .id(match.getMatchId())
                .score(match.getFullCourtGoal())
                .matchDate(match.getMatchDate())
                .build();
    }


    public LambdaQueryWrapper<SubMatchInfo> buildMatchInfoQuery() {
        LambdaQueryWrapper<SubMatchInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SubMatchInfo::getMatchStatus, "2");
        return queryWrapper;
    }
}
