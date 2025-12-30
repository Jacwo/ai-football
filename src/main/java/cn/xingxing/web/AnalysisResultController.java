package cn.xingxing.web;


import cn.xingxing.domain.AiAnalysisResult;
import cn.xingxing.dto.AnalysisPageDTO;
import cn.xingxing.dto.ApiResponse;
import cn.xingxing.dto.PageVO;
import cn.xingxing.service.AiAnalysisResultService;
import cn.xingxing.vo.AiAnalysisResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
/**
 * @Author: yangyuanliang
 * @Date: 2025-12-30
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/analysis")
public class AnalysisResultController {
    @Autowired
    private AiAnalysisResultService aiAnalysisResultService;
    @PostMapping("/history/list")
    public ApiResponse<PageVO<AiAnalysisResult>> matchInfoHistoryList(@RequestBody AnalysisPageDTO analysisPageDTO) {
        PageVO<AiAnalysisResult> historyList =aiAnalysisResultService.matchInfoHistoryList(analysisPageDTO);
        return ApiResponse.success(historyList);
    }


    @GetMapping("/history/{matchId}")
    public ApiResponse<AiAnalysisResultVo> getHistoryById(@PathVariable String matchId) {
        AiAnalysisResultVo byMatchId = aiAnalysisResultService.findByMatchId(matchId);
        return ApiResponse.success(byMatchId);
    }
}
