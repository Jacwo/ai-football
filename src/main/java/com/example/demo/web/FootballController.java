package com.example.demo.web;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.FootballAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutorService;

@Slf4j
@RestController
@RequestMapping("/api/match")
public class FootballController {

    @Autowired
    private FootballAnalysisService analysisService;

    @Autowired
    private ExecutorService footballExecutor;

    /**
     * 定时分析任务（每4小时执行一次）
     */
   /* @Scheduled(initialDelayString = "${football.api.schedule-initial-delay:10000}",
            fixedDelayString = "${football.api.schedule-fixed-delay:14400000}")
 */   public void scheduledAnalysis() {
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
}