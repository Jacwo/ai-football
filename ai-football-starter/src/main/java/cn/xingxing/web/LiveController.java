package cn.xingxing.web;


import cn.hutool.core.collection.CollectionUtil;
import cn.xingxing.common.config.FootballApiConfig;
import cn.xingxing.common.util.HttpClientUtil;
import cn.xingxing.dto.ApiResponse;
import cn.xingxing.dto.user.BatchCheckDto;
import cn.xingxing.dto.user.BatchCheckResponseDto;
import cn.xingxing.service.MatchInfoService;
import cn.xingxing.vo.LiveMatchVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: yangyuanliang
 * @Date: 2026-04-01
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/match")
public class LiveController {
    @Autowired
    private MatchInfoService matchInfoService;

    @Autowired
    private FootballApiConfig footballApiConfig;

    @PostMapping("/live")
    public ApiResponse<List<LiveMatchVo>> live() {
        try {
            // 查询所有未完成的比赛id
            List<Integer> unfinishedMatchIds = matchInfoService.getUnfinishedMatchIds();

            if (CollectionUtil.isEmpty(unfinishedMatchIds)) {
                log.info("当前没有未完成的比赛");
                return ApiResponse.success(Collections.emptyList());
            }

            // 将比赛id转换为逗号分隔的字符串
            String matchIdsStr = unfinishedMatchIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            // 构建直播查询URL
            String liveUrl = String.format(footballApiConfig.getLiveUrl(), matchIdsStr);
            log.info("查询直播信息，URL: {}, 比赛数量: {}", liveUrl, unfinishedMatchIds.size());

            // 调用API查询直播信息
            String response = HttpClientUtil.doGet(liveUrl, 30000);

            if (response == null) {
                log.error("查询直播信息失败，返回为空");
                return ApiResponse.success(Collections.emptyList());
            }

            // 解析响应
            JSONObject jsonObject = JSON.parseObject(response);
            if (jsonObject == null || !jsonObject.getBooleanValue("success")) {
                log.error("查询直播信息失败，响应: {}", response);
                return ApiResponse.success(Collections.emptyList());
            }

            // 提取value字段并转换为LiveMatchVo列表
            List<LiveMatchVo> liveMatches = JSON.parseArray(
                    jsonObject.getString("value"),
                    LiveMatchVo.class
            );

            log.info("成功查询到 {} 场比赛的直播信息", liveMatches != null ? liveMatches.size() : 0);
            return ApiResponse.success(liveMatches != null ? liveMatches : Collections.emptyList());

        } catch (Exception e) {
            log.error("查询直播信息异常", e);
            return ApiResponse.success(Collections.emptyList());
        }
    }
}
