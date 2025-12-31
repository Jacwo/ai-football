package cn.xingxing.web;

import cn.xingxing.domain.HadList;
import cn.xingxing.domain.SubMatchInfo;
import cn.xingxing.dto.AnalysisResultDto;
import cn.xingxing.dto.ApiResponse;
import cn.xingxing.dto.MatchAnalysis;
import cn.xingxing.service.FootballAnalysisService;
import cn.xingxing.service.HadListService;
import cn.xingxing.service.MatchInfoService;
import cn.xingxing.vo.MatchInfoVo;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Slf4j
@RestController
@RequestMapping("/api/match")
public class FootballController {

    @Autowired
    private FootballAnalysisService analysisService;

    @Autowired
    private ExecutorService footballExecutor;


    @Autowired
    private MatchInfoService matchInfoService;

    @Autowired
    private HadListService hadListService;

    /**
     * 定时分析任务（每4小时执行一次）
     */
    @Scheduled(initialDelayString = "${football.api.schedule-initial-delay:10000}", fixedDelayString = "${football.api.schedule-fixed-delay:14400000}")
    public void scheduledAnalysis() {
        log.info("定时分析任务启动");

        footballExecutor.execute(() -> {
            try {
                analysisService.analyzeAndNotify("ai");
            } catch (Exception e) {
                log.error("定时分析任务异常", e);
            }
        });
    }

    /**
     * 手动触发分析
     */
    @PostMapping("/analyze/{aiFlag}")
    public ApiResponse<String> triggerAnalysis(@PathVariable String aiFlag) {
        log.info("手动触发分析，AI标志: {}", aiFlag);

        footballExecutor.execute(() -> {
            try {
                analysisService.analyzeAndNotify(aiFlag);
            } catch (Exception e) {
                log.error("手动触发分析异常", e);
            }
        });

        return ApiResponse.success("分析任务已启动");
    }



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
        if(!CollectionUtils.isEmpty(currentDateMatch)) {
            matchInfoVos = JSONObject.parseArray(JSONObject.toJSONString(currentDateMatch),MatchInfoVo.class);
        }
        return ApiResponse.success(matchInfoVos);
    }
    @GetMapping("/{matchId}")
    public ApiResponse<MatchInfoVo> findMatchById(@PathVariable String matchId) {
        MatchInfoVo matchInfoVo = new MatchInfoVo();
        SubMatchInfo matchById = matchInfoService.findMatchById(matchId);
        BeanUtils.copyProperties(matchById,matchInfoVo);
        return ApiResponse.success(matchInfoVo);
    }




    @PostMapping("/analysis/{matchId}")
    public ApiResponse<AnalysisResultDto> analysisByMatchId(@PathVariable String matchId) {
        MatchAnalysis matchAnalysis = analysisService.analysisByMatchId(matchId);
        AnalysisResultDto build = AnalysisResultDto.builder().aiAnalysis(matchAnalysis.getAiAnalysis()).timestamp(matchAnalysis.getTimestamp()).build();
        return ApiResponse.success(build);

    }

}