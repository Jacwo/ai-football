package cn.xingxing.web;


import cn.xingxing.data.TeamStatsService;
import cn.xingxing.dto.TeamStatsVo;
import cn.xingxing.entity.*;
import cn.xingxing.dto.AnalysisResultDto;
import cn.xingxing.dto.ApiResponse;
import cn.xingxing.dto.MatchAnalysis;
import cn.xingxing.service.*;
import cn.xingxing.vo.MatchInfoVo;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/match")
public class FootballController {

    @Autowired
    private FootballAnalysisService analysisService;

    @Autowired
    private MatchInfoService matchInfoService;

    @Autowired
    private HistoricalMatchService historicalMatchService;

    @Autowired
    private SimilarMatchService similarMatchService;

    @Autowired
    private TeamStatsService teamStatsService;

    @Autowired
    private InformationService informationService;

    @Autowired
    private HadListService hadListService;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        return ApiResponse.success("服务运行正常");
    }

    @GetMapping("/list")
    public ApiResponse<List<MatchInfoVo>> listMatchInfo() {
        List<MatchInfoVo> matchInfoVos = new ArrayList<>();
        List<SubMatchInfo> currentDateMatch = matchInfoService.findMatchList();
        if (!CollectionUtils.isEmpty(currentDateMatch)) {
            matchInfoVos = JSONObject.parseArray(JSONObject.toJSONString(currentDateMatch), MatchInfoVo.class);
        }
        return ApiResponse.success(matchInfoVos);
    }

    @GetMapping("/{matchId}")
    public ApiResponse<MatchInfoVo> findMatchById(@PathVariable String matchId) {
        MatchInfoVo matchInfoVo = new MatchInfoVo();
        SubMatchInfo matchById = matchInfoService.findMatchById(matchId);
        BeanUtils.copyProperties(matchById, matchInfoVo);
        return ApiResponse.success(matchInfoVo);
    }

    @PostMapping("/analysis/{matchId}")
    public ApiResponse<AnalysisResultDto> analysisByMatchId(@PathVariable String matchId) {
        MatchAnalysis matchAnalysis = analysisService.analysisByMatchId(matchId);
        AnalysisResultDto build = AnalysisResultDto.builder().aiAnalysis(matchAnalysis.getAiAnalysis()).timestamp(matchAnalysis.getTimestamp()).build();
        return ApiResponse.success(build);
    }


    @PostMapping("/xg/data/{matchId}")
    public ApiResponse<TeamStatsVo> getXgData(@PathVariable String matchId) {
        SubMatchInfo matchById = matchInfoService.findMatchById(matchId);
        TeamStats home = teamStatsService.selectByTeamName(matchById.getHomeTeamAbbName(), "home");
        TeamStats away = teamStatsService.selectByTeamName(matchById.getAwayTeamAbbName(), "away");
      //  TeamStats all = teamStatsService.selectByTeamName(matchById.getHomeTeamAbbName(), "all");
        return ApiResponse.success(TeamStatsVo.builder().all(null).away(away).home(home).build());
    }


    @PostMapping("/history/data/{matchId}")
    public ApiResponse<List<HistoricalMatch>> getHistoryData(@PathVariable String matchId) {
        List<HistoricalMatch> historicalMatch = historicalMatchService.findHistoricalMatch(matchId);
        return ApiResponse.success(historicalMatch);
    }


    @PostMapping("/similar/data/{matchId}")
    public ApiResponse<List<SimilarMatch>> getSimilarData(@PathVariable String matchId) {
        List<SimilarMatch> similarMatch = similarMatchService.findSimilarMatch(matchId);
        return ApiResponse.success(similarMatch);
    }


    @PostMapping("/information/data/{matchId}")
    public ApiResponse<String> getInformationData(@PathVariable String matchId) {
        Information byId = informationService.getById(matchId);
        if (byId != null) {
            return ApiResponse.success(byId.getInfo());

        }
        return ApiResponse.success(null);
    }


    @PostMapping("/odds/data/{matchId}")
    public ApiResponse<List<HadList>> getOddsData(@PathVariable String matchId) {
        List<HadList> hadList = hadListService.findHadList(matchId);
        return ApiResponse.success(hadList);
    }

}