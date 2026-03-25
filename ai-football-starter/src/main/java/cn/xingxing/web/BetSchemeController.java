package cn.xingxing.web;

import cn.xingxing.dto.ApiResponse;
import cn.xingxing.dto.BetSchemeSaveDto;
import cn.xingxing.dto.BetSchemeVo;
import cn.xingxing.service.BetSchemeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 投注方案控制器
 * @Author: yangyuanliang
 * @Date: 2026-03-24
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/match/calculator")
public class BetSchemeController {

    @Autowired
    private BetSchemeService betSchemeService;

    /**
     * 保存投注方案
     * @param saveDto 保存参数
     * @return 方案编号
     */
    @PostMapping("/save")
    public ApiResponse<String> saveBetScheme(@RequestBody BetSchemeSaveDto saveDto) {
        log.info("保存投注方案, 用户ID: {}, 选择数量: {}", saveDto.getUserId(), saveDto.getSelections().size());
        String schemeNo = betSchemeService.saveBetScheme(saveDto);
        return ApiResponse.success(schemeNo);
    }

    /**
     * 查询用户方案列表
     * @param userId 用户ID
     * @return 方案列表
     */
    @GetMapping("/get/{userId}")
    public ApiResponse<List<BetSchemeVo>> getUserSchemes(@PathVariable String userId) {
        log.info("查询用户方案列表, 用户ID: {}", userId);
        List<BetSchemeVo> schemes = betSchemeService.getUserSchemes(userId);
        return ApiResponse.success(schemes);
    }

    @PostMapping("/delete/{id}")
    public ApiResponse<Boolean> deleteScheme(@PathVariable String id) {
        log.info("查询删除方案列表, id: {}", id);
        return ApiResponse.success(betSchemeService.deleteScheme(id));
    }
}
