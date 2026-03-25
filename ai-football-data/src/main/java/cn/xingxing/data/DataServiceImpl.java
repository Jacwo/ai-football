package cn.xingxing.data;


import cn.xingxing.common.config.FootballApiConfig;
import cn.xingxing.dto.*;
import cn.xingxing.dto.url5.Match;
import cn.xingxing.dto.url5.MatchInfo5;
import cn.xingxing.entity.*;
import cn.xingxing.mapper.*;
import cn.xingxing.dto.MatchResultDetailResponse;
import cn.xingxing.common.util.HttpClientUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
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

    @Autowired
    private MatchCalculatorMapper matchCalculatorMapper;

    @Autowired
    private MatchResultDetailMapper matchResultDetailMapper;

    @Autowired
    private BetSchemeOptionMapper betSchemeOptionMapper;

    @Autowired
    private BetSchemeMapper betSchemeMapper;

    @Autowired
    private BetSchemeDetailMapper betSchemeDetailMapper;


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
        // 1. 查询需要更新结果的AI分析记录(近5天到明天)
        LambdaQueryWrapper<AiAnalysisResult> queryWrapper = new LambdaQueryWrapper<>();
        LocalDate localDate = LocalDate.now();
        queryWrapper.between(AiAnalysisResult::getMatchTime, localDate.minusDays(5), localDate.plusDays(1));
        List<AiAnalysisResult> aiAnalysisResults = aiAnalysisResultMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(aiAnalysisResults)) {
            log.info("没有需要更新结果的AI分析记录");
            return 0;
        }

        // 2. 遍历每个比赛，调用matchResultDetailUrl获取详细结果
        List<Integer> completedMatchIds = new ArrayList<>();

        for (AiAnalysisResult analysisResult : aiAnalysisResults) {
            try {
                Integer matchId = Integer.valueOf(analysisResult.getMatchId());

                // 调用详情接口获取比赛结果
                MatchResultDetail matchResultDetail = getMatchResultDetail(matchId);

                if (matchResultDetail != null) {
                    // 3. 保存完整的比赛结果到match_result_detail表
                    saveOrUpdateMatchResultDetail(matchResultDetail);

                    // 4. 只更新ai_analysis_result表的比分字段
                    analysisResult.setMatchResult(matchResultDetail.getSectionsNo999());
                    aiAnalysisResultMapper.updateById(analysisResult);

                    // 5. 更新投注方案选项的中奖状态
                    updateBetSchemeOptions(matchResultDetail);

                    completedMatchIds.add(matchId);

                    log.info("更新比赛结果成功: matchId={}, score={}", matchId, matchResultDetail.getSectionsNo999());
                }
            } catch (Exception e) {
                log.error("更新比赛结果失败: matchId={}, error={}", analysisResult.getMatchId(), e.getMessage(), e);
            }
        }

        // 6. 批量更新match_info表的状态为已完成
        if (!CollectionUtils.isEmpty(completedMatchIds)) {
            LambdaUpdateWrapper<SubMatchInfo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(SubMatchInfo::getMatchId, completedMatchIds);
            updateWrapper.set(SubMatchInfo::getMatchStatus, "3");
            updateWrapper.set(SubMatchInfo::getMatchStatusName, "已完成");
            matchInfoMapper.update(updateWrapper);
            log.info("批量更新比赛状态完成,共{}场比赛", completedMatchIds.size());
        }

        return completedMatchIds.size();
    }

    /**
     * 获取比赛结果详情
     * @param matchId 比赛ID
     * @return 比赛结果详情
     */
    private MatchResultDetail getMatchResultDetail(Integer matchId) {
        try {
            String url = String.format(apiConfig.getMatchResultDetailUrl(), matchId);
            String response = HttpClientUtil.doGet(url, apiConfig.getHttpConnectTimeout());

            MatchResultDetailResponse matchResultDetailResponse = JSONObject.parseObject(response, MatchResultDetailResponse.class);

            if (matchResultDetailResponse != null && matchResultDetailResponse.getSuccess()) {
                MatchResultDetailResponse.MatchResultDetailValue value = matchResultDetailResponse.getValue();

                if (value != null) {
                    return buildMatchResultDetail(value);
                }
            }
        } catch (Exception e) {
            log.error("获取比赛结果详情失败: matchId={}", matchId, e);
        }

        return null;
    }

    /**
     * 构建比赛结果详情实体
     */
    private MatchResultDetail buildMatchResultDetail(MatchResultDetailResponse.MatchResultDetailValue value) {
        MatchResultDetail detail = MatchResultDetail.builder()
                .matchId(value.getMatchId())
                .matchStatus(value.getMatchStatus())
                .matchStatusName(value.getMatchStatusName())
                .matchMinute(value.getMatchMinute())
                .matchMinuteExtra(value.getMatchMinuteExtra())
                .matchPhaseTc(value.getMatchPhaseTc())
                .matchPhaseTcName(value.getMatchPhaseTcName())
                .sectionsNo1(value.getSectionsNo1())
                .sectionsNo999(value.getSectionsNo999())
                .sectionsExtra(value.getSectionsExtra())
                .sectionsPenalty(value.getSectionsPenalty())
                .build();

        // 解析各玩法开奖结果
        if (!CollectionUtils.isEmpty(value.getMatchResultList())) {
            for (MatchResultDetailResponse.MatchResultItem item : value.getMatchResultList()) {
                String poolCode = item.getPoolCode();

                switch (poolCode) {
                    case "HAD":
                        detail.setHadResult(item.getCombination());
                        detail.setHadCombinationDesc(item.getCombinationDesc());
                        detail.setHadOdds(new java.math.BigDecimal(item.getOdds()));
                        break;
                    case "HHAD":
                        detail.setHhadResult(item.getCombination());
                        detail.setHhadCombinationDesc(item.getCombinationDesc());
                        detail.setHhadOdds(new java.math.BigDecimal(item.getOdds()));
                        break;
                    case "TTG":
                        detail.setTtgResult(item.getCombination());
                        detail.setTtgCombinationDesc(item.getCombinationDesc());
                        detail.setTtgOdds(new java.math.BigDecimal(item.getOdds()));
                        break;
                    case "HAFU":
                        detail.setHafuResult(item.getCombination());
                        detail.setHafuCombinationDesc(item.getCombinationDesc());
                        detail.setHafuOdds(new java.math.BigDecimal(item.getOdds()));
                        break;
                    case "CRS":
                        detail.setCrsResult(item.getCombination());
                        detail.setCrsCombinationDesc(item.getCombinationDesc());
                        detail.setCrsOdds(new java.math.BigDecimal(item.getOdds()));
                        break;
                    default:
                        log.warn("未知的玩法类型: {}", poolCode);
                }
            }
        }

        return detail;
    }

    /**
     * 保存或更新比赛结果详情
     */
    private void saveOrUpdateMatchResultDetail(MatchResultDetail detail) {
        MatchResultDetail existing = matchResultDetailMapper.selectById(detail.getMatchId());

        if (existing == null) {
            matchResultDetailMapper.insert(detail);
        } else {
            matchResultDetailMapper.updateById(detail);
        }
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

    @Override
    public void syncMatchCalculator() {
        try {
            String url = apiConfig.getMatchCalculatorUrl();
            String response = HttpClientUtil.doGet(url, apiConfig.getHttpConnectTimeout());

            MatchCalculatorResponse matchCalculatorResponse = JSONObject.parseObject(response, MatchCalculatorResponse.class);
            MatchCalculatorValue value = matchCalculatorResponse.getValue();

            if (value != null && !CollectionUtils.isEmpty(value.getMatchInfoList())) {
                List<MatchCalculator> matchCalculators = value.getMatchInfoList().stream()
                        .flatMap(info -> info.getSubMatchList().stream())
                        .map(this::convertToMatchCalculator)
                        .collect(Collectors.toList());

                // 批量插入或更新到数据库
                if (!CollectionUtils.isEmpty(matchCalculators)) {
                    matchCalculators.forEach(matchCalculator -> {
                        LambdaQueryWrapper<MatchCalculator> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(MatchCalculator::getMatchId,matchCalculator.getMatchId());
                        MatchCalculator dbMatchCalculator = matchCalculatorMapper.selectOne(queryWrapper);
                        if(dbMatchCalculator==null){
                            matchCalculatorMapper.insert(matchCalculator);

                        }else{
                            BeanUtils.copyProperties(matchCalculator,dbMatchCalculator);
                            matchCalculatorMapper.updateById(dbMatchCalculator);
                        }
                    });
                    log.info("同步比赛计算器数据成功,共{}条", matchCalculators.size());
                }
            }
        } catch (Exception e) {
            log.error("同步比赛计算器数据失败", e);
        }
    }

    /**
     * 将SubMatchCalculator转换为MatchCalculator实体
     */
    private MatchCalculator convertToMatchCalculator(SubMatchCalculator subMatch) {
        MatchCalculator calculator = MatchCalculator.builder()
                .matchId(subMatch.getMatchId())
                .matchNum(subMatch.getMatchNum())
                .matchNumStr(subMatch.getMatchNumStr())
                .matchNumDate(subMatch.getMatchNumDate())
                .matchDate(subMatch.getMatchDate())
                .matchTime(subMatch.getMatchTime())
                .matchWeek(subMatch.getMatchWeek())
                .homeTeamId(subMatch.getHomeTeamId())
                .homeTeamAbbName(subMatch.getHomeTeamAbbName())
                .homeTeamAbbEnName(subMatch.getHomeTeamAbbEnName())
                .homeTeamAllName(subMatch.getHomeTeamAllName())
                .homeTeamCode(subMatch.getHomeTeamCode())
                .homeRank(subMatch.getHomeRank())
                .awayTeamId(subMatch.getAwayTeamId())
                .awayTeamAbbName(subMatch.getAwayTeamAbbName())
                .awayTeamAbbEnName(subMatch.getAwayTeamAbbEnName())
                .awayTeamAllName(subMatch.getAwayTeamAllName())
                .awayTeamCode(subMatch.getAwayTeamCode())
                .awayRank(subMatch.getAwayRank())
                .leagueId(subMatch.getLeagueId())
                .leagueAbbName(subMatch.getLeagueAbbName())
                .leagueAllName(subMatch.getLeagueAllName())
                .leagueCode(subMatch.getLeagueCode())
                .matchStatus(subMatch.getMatchStatus())
                .sellStatus(subMatch.getSellStatus())
                .backColor(subMatch.getBackColor())
                .businessDate(subMatch.getBusinessDate())
                .isHide(subMatch.getIsHide())
                .isHot(subMatch.getIsHot())
                .taxDateNo(subMatch.getTaxDateNo())
                .bettingSingle(subMatch.getBettingSingle())
                .bettingAllUp(subMatch.getBettingAllUp())
                .build();

        // 设置HAD胜平负数据
        if (subMatch.getHad() != null) {
            HadOdds had = subMatch.getHad();
            calculator.setHadH(had.getH());
            calculator.setHadD(had.getD());
            calculator.setHadA(had.getA());
            calculator.setHadHf(had.getHf());
            calculator.setHadDf(had.getDf());
            calculator.setHadAf(had.getAf());
            calculator.setHadUpdateDate(had.getUpdateDate());
            calculator.setHadUpdateTime(had.getUpdateTime());
        }

        // 设置HHAD让球胜平负数据
        if (subMatch.getHhad() != null) {
            HhadOdds hhad = subMatch.getHhad();
            calculator.setHhadH(hhad.getH());
            calculator.setHhadD(hhad.getD());
            calculator.setHhadA(hhad.getA());
            calculator.setHhadGoalLine(hhad.getGoalLine());
            calculator.setHhadGoalLineValue(hhad.getGoalLineValue());
            calculator.setHhadHf(hhad.getHf());
            calculator.setHhadDf(hhad.getDf());
            calculator.setHhadAf(hhad.getAf());
            calculator.setHhadUpdateDate(hhad.getUpdateDate());
            calculator.setHhadUpdateTime(hhad.getUpdateTime());
        }

        // 设置TTG总进球数据
        if (subMatch.getTtg() != null) {
            TtgOdds ttg = subMatch.getTtg();
            calculator.setTtgS0(ttg.getS0());
            calculator.setTtgS1(ttg.getS1());
            calculator.setTtgS2(ttg.getS2());
            calculator.setTtgS3(ttg.getS3());
            calculator.setTtgS4(ttg.getS4());
            calculator.setTtgS5(ttg.getS5());
            calculator.setTtgS6(ttg.getS6());
            calculator.setTtgS7(ttg.getS7());
            calculator.setTtgUpdateDate(ttg.getUpdateDate());
            calculator.setTtgUpdateTime(ttg.getUpdateTime());
        }

        // 设置HAFU半全场数据
        if (subMatch.getHafu() != null) {
            HafuOdds hafu = subMatch.getHafu();
            calculator.setHafuHh(hafu.getHh());
            calculator.setHafuHd(hafu.getHd());
            calculator.setHafuHa(hafu.getHa());
            calculator.setHafuDh(hafu.getDh());
            calculator.setHafuDd(hafu.getDd());
            calculator.setHafuDa(hafu.getDa());
            calculator.setHafuAh(hafu.getAh());
            calculator.setHafuAd(hafu.getAd());
            calculator.setHafuAa(hafu.getAa());
            calculator.setHafuHhf(hafu.getHhf());
            calculator.setHafuHdf(hafu.getHdf());
            calculator.setHafuHaf(hafu.getHaf());
            calculator.setHafuDhf(hafu.getDhf());
            calculator.setHafuDdf(hafu.getDdf());
            calculator.setHafuDaf(hafu.getDaf());
            calculator.setHafuAhf(hafu.getAhf());
            calculator.setHafuAdf(hafu.getAdf());
            calculator.setHafuAaf(hafu.getAaf());
            calculator.setHafuUpdateDate(hafu.getUpdateDate());
            calculator.setHafuUpdateTime(hafu.getUpdateTime());
        }

        // 设置CRS比分数据
        if (subMatch.getCrs() != null) {
            CrsOdds crs = subMatch.getCrs();
            calculator.setCrsS00s00(crs.getS00s00());
            calculator.setCrsS00s01(crs.getS00s01());
            calculator.setCrsS00s02(crs.getS00s02());
            calculator.setCrsS00s03(crs.getS00s03());
            calculator.setCrsS00s04(crs.getS00s04());
            calculator.setCrsS00s05(crs.getS00s05());
            calculator.setCrsS01s00(crs.getS01s00());
            calculator.setCrsS01s01(crs.getS01s01());
            calculator.setCrsS01s02(crs.getS01s02());
            calculator.setCrsS01s03(crs.getS01s03());
            calculator.setCrsS01s04(crs.getS01s04());
            calculator.setCrsS01s05(crs.getS01s05());
            calculator.setCrsS02s00(crs.getS02s00());
            calculator.setCrsS02s01(crs.getS02s01());
            calculator.setCrsS02s02(crs.getS02s02());
            calculator.setCrsS02s03(crs.getS02s03());
            calculator.setCrsS02s04(crs.getS02s04());
            calculator.setCrsS02s05(crs.getS02s05());
            calculator.setCrsS03s00(crs.getS03s00());
            calculator.setCrsS03s01(crs.getS03s01());
            calculator.setCrsS03s02(crs.getS03s02());
            calculator.setCrsS03s03(crs.getS03s03());
            calculator.setCrsS04s00(crs.getS04s00());
            calculator.setCrsS04s01(crs.getS04s01());
            calculator.setCrsS04s02(crs.getS04s02());
            calculator.setCrsS05s00(crs.getS05s00());
            calculator.setCrsS05s01(crs.getS05s01());
            calculator.setCrsS05s02(crs.getS05s02());
            calculator.setCrsS1sh(crs.getS1sh());
            calculator.setCrsS1sd(crs.getS1sd());
            calculator.setCrsS1sa(crs.getS1sa());
            calculator.setCrsUpdateDate(crs.getUpdateDate());
            calculator.setCrsUpdateTime(crs.getUpdateTime());
        }

        return calculator;
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
    @Override
    public MatchHistoryData getMatchHistoryData(String matchId) {
        String url = String.format(apiConfig.getMatchHistoryUrl(), matchId);
        String response = HttpClientUtil.doGet(url, apiConfig.getHttpConnectTimeout());
        MatchHistoryResponse matchAnalysisResponse = JSONObject.parseObject(response, MatchHistoryResponse.class);
        return matchAnalysisResponse.getValue();

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

    /**
     * 更新投注方案选项的中奖状态，并判断方案整体是否中奖
     * @param matchResultDetail 比赛结果详情
     */
    private void updateBetSchemeOptions(MatchResultDetail matchResultDetail) {
        try {
            Integer matchId = matchResultDetail.getMatchId();

            // 查询该比赛相关的所有投注选项
            LambdaQueryWrapper<BetSchemeOption> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BetSchemeOption::getMatchId, matchId);
            List<BetSchemeOption> betSchemeOptions = betSchemeOptionMapper.selectList(queryWrapper);

            if (CollectionUtils.isEmpty(betSchemeOptions)) {
                log.debug("比赛 {} 没有相关投注选项", matchId);
                return;
            }

            // 遍历每个投注选项，判断是否中奖
            int hitCount = 0;
            int missCount = 0;
            java.time.LocalDateTime checkTime = java.time.LocalDateTime.now();

            // 收集需要更新状态的方案ID
            Set<Long> affectedSchemeIds = new HashSet<>();

            for (BetSchemeOption option : betSchemeOptions) {
                String optionType = option.getOptionType().toLowerCase();
                String optionValue = option.getOptionValue();
                boolean isHit = false;
                String matchResult = null;
                String matchResultDesc = null;
                BigDecimal resultOdds = null;

                // 根据不同的玩法类型判断是否命中
                switch (optionType) {
                    case "had": // 胜平负
                        matchResult = matchResultDetail.getHadResult();
                        matchResultDesc = matchResultDetail.getHadCombinationDesc();
                        resultOdds = matchResultDetail.getHadOdds();
                        if (matchResult != null) {
                            isHit = optionValue.equalsIgnoreCase(matchResult);
                        }
                        break;

                    case "hhad": // 让球胜平负
                        matchResult = matchResultDetail.getHhadResult();
                        matchResultDesc = matchResultDetail.getHhadCombinationDesc();
                        resultOdds = matchResultDetail.getHhadOdds();
                        if (matchResult != null) {
                            isHit = optionValue.equalsIgnoreCase(matchResult);
                        }
                        break;

                    case "ttg": // 总进球
                        matchResult = matchResultDetail.getTtgResult();
                        matchResultDesc = matchResultDetail.getTtgCombinationDesc();
                        resultOdds = matchResultDetail.getTtgOdds();
                        if (matchResult != null) {
                            isHit = optionValue.equals(matchResult);
                        }
                        break;

                    case "hafu": // 半全场
                        matchResult = matchResultDetail.getHafuResult();
                        matchResultDesc = matchResultDetail.getHafuCombinationDesc();
                        resultOdds = matchResultDetail.getHafuOdds();
                        if (matchResult != null) {
                            // 半全场格式如 "H:H", "D:A" 等，去除冒号比较
                            isHit = optionValue.equalsIgnoreCase(matchResult.replace(":", ""));
                        }
                        break;

                    case "crs": // 比分
                        matchResult = matchResultDetail.getCrsResult();
                        matchResultDesc = matchResultDetail.getCrsCombinationDesc();
                        resultOdds = matchResultDetail.getCrsOdds();
                        if (matchResult != null) {
                            isHit = optionValue.equals(matchResult);
                        }
                        break;

                    default:
                        log.warn("未知的玩法类型: optionType={}, matchId={}", optionType, matchId);
                        continue;
                }

                // 更新投注选项
                option.setIsHit(isHit ? 1 : 0);
                option.setMatchResult(matchResult);
                option.setMatchResultDesc(matchResultDesc);
                option.setResultOdds(resultOdds);
                option.setCheckTime(checkTime);

                betSchemeOptionMapper.updateById(option);

                // 收集受影响的方案ID
                affectedSchemeIds.add(option.getSchemeId());

                if (isHit) {
                    hitCount++;
                } else {
                    missCount++;
                }

                log.debug("更新投注选项: matchId={}, schemeId={}, optionType={}, optionValue={}, isHit={}, matchResult={}",
                        matchId, option.getSchemeId(), optionType, optionValue, isHit, matchResult);
            }

            log.info("更新投注选项完成: matchId={}, 总数={}, 命中={}, 未命中={}",
                    matchId, betSchemeOptions.size(), hitCount, missCount);

            // 遍历所有受影响的方案，判断方案整体是否中奖
            for (Long schemeId : affectedSchemeIds) {
                checkAndUpdateSchemeStatus(schemeId);
            }

        } catch (Exception e) {
            log.error("更新投注选项失败: matchId={}, error={}", matchResultDetail.getMatchId(), e.getMessage(), e);
        }
    }

    /**
     * 检查并更新方案的中奖状态
     * @param schemeId 方案ID
     */
    private void checkAndUpdateSchemeStatus(Long schemeId) {
        try {
            // 1. 查询方案信息
            BetScheme betScheme = betSchemeMapper.selectById(schemeId);
            if (betScheme == null) {
                log.warn("方案不存在: schemeId={}", schemeId);
                return;
            }

            // 2. 查询方案的所有比赛明细
            LambdaQueryWrapper<BetSchemeDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(BetSchemeDetail::getSchemeId, schemeId);
            List<BetSchemeDetail> details = betSchemeDetailMapper.selectList(detailWrapper);

            if (CollectionUtils.isEmpty(details)) {
                log.warn("方案没有明细: schemeId={}", schemeId);
                return;
            }

            // 3. 查询方案的所有投注选项
            LambdaQueryWrapper<BetSchemeOption> optionWrapper = new LambdaQueryWrapper<>();
            optionWrapper.eq(BetSchemeOption::getSchemeId, schemeId);
            List<BetSchemeOption> allOptions = betSchemeOptionMapper.selectList(optionWrapper);

            // 4. 按照比赛分组统计每场比赛的命中情况
            Map<Long, List<BetSchemeOption>> matchOptionsMap = allOptions.stream()
                    .collect(Collectors.groupingBy(BetSchemeOption::getMatchId));

            // 统计每场比赛的命中状态
            int totalMatches = matchOptionsMap.size();
            int hitMatches = 0;      // 命中的比赛数
            int missMatches = 0;     // 未命中的比赛数
            int pendingMatches = 0;  // 未开奖的比赛数

            for (Map.Entry<Long, List<BetSchemeOption>> entry : matchOptionsMap.entrySet()) {
                List<BetSchemeOption> matchOptions = entry.getValue();

                // 判断该场比赛是否有选项命中
                boolean hasHit = matchOptions.stream().anyMatch(opt -> opt.getIsHit() != null && opt.getIsHit() == 1);
                boolean hasUnchecked = matchOptions.stream().anyMatch(opt -> opt.getIsHit() == null);

                if (hasUnchecked) {
                    pendingMatches++;
                } else if (hasHit) {
                    hitMatches++;
                } else {
                    missMatches++;
                }
            }

            log.debug("方案统计: schemeId={}, 总场次={}, 命中={}, 未中={}, 待开奖={}",
                    schemeId, totalMatches, hitMatches, missMatches, pendingMatches);

            // 5. 如果还有比赛未开奖，保持待开奖状态
            if (pendingMatches > 0) {
                log.debug("方案还有{}场比赛未开奖，保持待开奖状态: schemeId={}", pendingMatches, schemeId);
                return;
            }

            // 6. 所有比赛都已开奖，根据过关方式判断是否中奖
            String passTypes = betScheme.getPassTypes();
            boolean isWin = checkPassTypeWin(passTypes, totalMatches, hitMatches);

            // 7. 更新方案状态
            Integer newStatus = isWin ? 1 : 2; // 1-已中奖, 2-未中奖
            if (!newStatus.equals(betScheme.getStatus())) {
                betScheme.setStatus(newStatus);
                betScheme.setUpdateTime(java.time.LocalDateTime.now());
                betSchemeMapper.updateById(betScheme);

                log.info("更新方案状态: schemeId={}, schemeNo={}, passTypes={}, status={}, totalMatches={}, hitMatches={}",
                        schemeId, betScheme.getSchemeNo(), passTypes, newStatus == 1 ? "已中奖" : "未中奖",
                        totalMatches, hitMatches);
            }

        } catch (Exception e) {
            log.error("检查方案状态失败: schemeId={}, error={}", schemeId, e.getMessage(), e);
        }
    }

    /**
     * 根据过关方式判断是否中奖
     * @param passTypes 过关方式，多个用逗号分隔(如: single, 2_1, 3_1)
     * @param totalMatches 总场次
     * @param hitMatches 命中场次
     * @return true-中奖, false-未中奖
     */
    private boolean checkPassTypeWin(String passTypes, int totalMatches, int hitMatches) {
        if (passTypes == null || passTypes.isEmpty()) {
            return false;
        }

        // 分割多个过关方式
        String[] passTypeArray = passTypes.split(",");

        // 只要有一种过关方式满足条件就算中奖
        for (String passType : passTypeArray) {
            passType = passType.trim().toLowerCase();

            if (passType.equals("single") || passType.equals("1x1") || passType.equals("1_1")) {
                // 单关：只投注1场，命中即中奖
                if (totalMatches == 1 && hitMatches == 1) {
                    log.debug("单关中奖: passType={}", passType);
                    return true;
                }
            } else if (passType.contains("_") || passType.contains("x")) {
                // 串关：格式如 "2_1"(2串1) 或 "2x1"
                String[] parts = passType.split("[_x]");
                if (parts.length == 2) {
                    try {
                        int m = Integer.parseInt(parts[0]); // 选择的场次
                        int n = Integer.parseInt(parts[1]); // 串关数

                        // m串n的规则：
                        // 2串1: 2场都要中
                        // 3串1: 3场都要中
                        // 3串3: 至少2场要中(3选2组合)
                        // 3串4: 至少2场要中(3选2组合 + 3串1)
                        // 4串11: 至少2场要中(4选2组合 + 4选3组合 + 4串1)

                        if (n == 1) {
                            // m串1：m场全部命中才中奖
                            if (totalMatches >= m && hitMatches >= m) {
                                log.debug("{}串1中奖: totalMatches={}, hitMatches={}", m, totalMatches, hitMatches);
                                return true;
                            }
                        } else {
                            // m串n(n>1)：至少命中 m-1 场就有奖（容错1场）
                            // 简化规则：命中数 >= m-1 就算中奖
                            int minHitRequired = Math.max(2, m - 1);
                            if (totalMatches >= m && hitMatches >= minHitRequired) {
                                log.debug("{}串{}中奖: totalMatches={}, hitMatches={}, minRequired={}",
                                        m, n, totalMatches, hitMatches, minHitRequired);
                                return true;
                            }
                        }
                    } catch (NumberFormatException e) {
                        log.warn("解析过关方式失败: passType={}", passType);
                    }
                }
            }
        }

        log.debug("未中奖: passTypes={}, totalMatches={}, hitMatches={}", passTypes, totalMatches, hitMatches);
        return false;
    }
}
