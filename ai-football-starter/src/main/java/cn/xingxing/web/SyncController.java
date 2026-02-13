package cn.xingxing.web;


import cn.xingxing.data.DataService;
import cn.xingxing.dto.ApiResponse;
import cn.xingxing.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * @Author: yangyuanliang
 * @Date: 2025-12-30
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api")
public class SyncController {
    @Autowired
    private DataService dataService;

    @Autowired
    private AIService aiService;
    @GetMapping("/sync/match")
    public ApiResponse<Boolean> syncMatchInfo(){
        dataService.syncMatchInfoData();
        return ApiResponse.success(true);
    }

    @GetMapping("/sync/hadList")
    public ApiResponse<Boolean> syncHadInfo(){
        dataService.syncHadListData();
        return ApiResponse.success(true);
    }



    @GetMapping("/sync/similar/match")
    public ApiResponse<Boolean> syncSimilarMatch(){
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        executorService.execute(() -> dataService.syncSimilarMatch());
        return ApiResponse.success(true);
    }


    @GetMapping("/sync/history/match")
    public ApiResponse<Boolean> syncHistoryMatch(){
        dataService.syncHistoryData();
        return ApiResponse.success(true);
    }


    @GetMapping("/sync/match/result")
    public ApiResponse<Boolean> syncMatchResult(){
        dataService.syncMatchResult();
        return ApiResponse.success(true);
    }


    @GetMapping("/after/match/analysis")
    public ApiResponse<Boolean> afterMatchAnalysis(){
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        executorService.execute(() -> aiService.afterMatchAnalysis());
        return ApiResponse.success(true);
    }
}
