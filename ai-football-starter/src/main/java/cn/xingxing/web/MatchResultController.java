package cn.xingxing.web;


import cn.xingxing.dto.ApiResponse;
import cn.xingxing.dto.MatchResultDetailDto;
import cn.xingxing.service.MatchResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2026-03-26
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api")
public class MatchResultController {
    @Autowired
    private MatchResultService matchResultService;

    @PostMapping("/match/result/list")
    public ApiResponse<List<MatchResultDetailDto>> listMatchResult() {
        return ApiResponse.success(matchResultService.listMatchResult());
    }
}
