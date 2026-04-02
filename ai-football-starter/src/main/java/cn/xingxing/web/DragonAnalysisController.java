package cn.xingxing.web;

import cn.xingxing.dto.ApiResponse;
import cn.xingxing.service.DragonAnalysisService;
import cn.xingxing.vo.DragonAnalysisVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 长龙分析Controller
 *
 * @Author: yangyuanliang
 * @Date: 2025-01-15
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/dragon/analysis")
public class DragonAnalysisController {

    @Autowired
    private DragonAnalysisService dragonAnalysisService;

    /**
     * 分析近N场单关比赛的长龙情况
     * @param sampleSize 样本数量（可选，默认30场）
     * @return 长龙分析结果
     */
    @GetMapping("/analyze")
    public ApiResponse<DragonAnalysisVO> analyzeDragon(
            @RequestParam(required = false, defaultValue = "30") Integer sampleSize) {
        try {
            log.info("开始分析近{}场单关比赛的长龙情况", sampleSize);
            DragonAnalysisVO result = dragonAnalysisService.analyzeDragon(sampleSize);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("长龙分析失败", e);
            return ApiResponse.error("长龙分析失败: " + e.getMessage());
        }
    }
}
