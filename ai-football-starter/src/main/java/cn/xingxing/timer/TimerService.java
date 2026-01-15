package cn.xingxing.timer;


import cn.xingxing.data.DataService;
import cn.xingxing.data.TeamStatsService;
import cn.xingxing.data.util.EPLDataGenerator;
import cn.xingxing.service.FootballAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-15
 * @Version: 1.0
 */
@Component
@Slf4j
public class TimerService{


    @Autowired
    private DataService dataService;


    @Autowired
    private TeamStatsService teamStatsService;

    @Autowired
    private ExecutorService footballExecutor;


    @Autowired
    private FootballAnalysisService analysisService;
    /**
     * 定时分析任务（每4小时执行一次）
     */
    @Scheduled(initialDelayString = "${football.api.schedule-initial-delay:10000}", fixedDelayString = "${football.api.schedule-fixed-delay:14400000}")
    public void scheduledAnalysis() {
        log.info("定时分析任务启动");

        footballExecutor.execute(() -> {
            try {
                analysisService.analyzeAndNotify();
            } catch (Exception e) {
                log.error("定时分析任务异常", e);
            }
        });
    }


    @Scheduled(initialDelayString = "${football.api.schedule-initial-delay:200000}", fixedDelayString = "${football.api.schedule-fixed-delay:360000}")
    public void syncMatchInfoData() {
        log.info("定时同步比赛信息启动");
        dataService.syncMatchInfoData();
        dataService.syncMatchResult();
    }



    @Scheduled(initialDelayString = "${football.api.schedule-initial-delay:20000}", fixedDelayString = "${football.api.schedule-fixed-delay:60000}")
    public void syncNeedData() {
        log.info("定时同步赔率信息启动");
        dataService.syncHadListData();
    }


    @Scheduled(initialDelayString = "${football.api.schedule-initial-delay:120000}", fixedDelayString = "${football.api.schedule-fixed-delay:260000}")
    public void syncSimilarMatch() {
        log.info("定时同步同奖信息启动");
        dataService.syncSimilarMatch();
    }


    @Scheduled(initialDelayString = "${football.api.schedule-initial-delay:24000}", fixedDelayString = "${football.api.schedule-fixed-delay:28800000}")
    public void syncXgData() {
        log.info("定时同步Xg信息启动");

        List<String> list = new ArrayList<>();
        list.add("EPL");//英超
        list.add("La_liga");//西甲
        list.add("Ligue_1");//法甲
        list.add("Bundesliga");//德甲
        list.add("Serie_A");//意甲
        list.forEach(league -> {
            teamStatsService.syncXgData(league, "2025");
            EPLDataGenerator.saveXgData(league);
            teamStatsService.loadTeamStats(league);
            teamStatsService.loadTeamStatsHome(league);
            teamStatsService.loadTeamStatsAway(league);
        });



    }
}
