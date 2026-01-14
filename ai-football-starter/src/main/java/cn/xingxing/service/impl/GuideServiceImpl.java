package cn.xingxing.service.impl;


import cn.xingxing.entity.AiAnalysisResult;
import cn.xingxing.entity.Guide;
import cn.xingxing.entity.SubMatchInfo;
import cn.xingxing.dto.ai.GuestAskDto;
import cn.xingxing.mapper.AiAnalysisResultMapper;
import cn.xingxing.mapper.GuideMapper;
import cn.xingxing.service.GuideService;
import cn.xingxing.service.MatchInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-06
 * @Version: 1.0
 */
@Service
public class GuideServiceImpl extends ServiceImpl<GuideMapper, Guide> implements GuideService {

    @Autowired
    private MatchInfoService matchInfoService;

    @Override
    public void initEngine() {
        this.remove(null);
        List<SubMatchInfo> matchList = matchInfoService.findMatchList();
        List<Guide> list = matchList.stream().map((p -> mergeToGuide(p))).toList();
        this.saveBatch(list);
    }

    private Guide mergeToGuide(SubMatchInfo p) {
        Guide guide = new Guide();
        guide.setMatchId(String.valueOf(p.getMatchId()));
        guide.setQuestionName(formatQuestionName(p));
        return guide;
    }

    private String formatQuestionName(SubMatchInfo p) {
        return String.format("分析%svs%s这场比赛",p.getHomeTeamAbbName(),p.getAwayTeamAbbName());
    }

    @Override
    public GuestAskDto loadGuestAsk() {
        // 或者方案2: 使用Stream API随机选择
        GuestAskDto  guestAskDto = new GuestAskDto();

        List<Guide> list = this.query().list();
        List<String> collect = list.stream()
                .map(Guide::getQuestionName)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        collected -> {
                            Collections.shuffle(collected);
                            return collected.stream().limit(3).collect(Collectors.toList());
                        }
                ));
        guestAskDto.setQuestionName(collect);
        return guestAskDto;
    }

    @Override
    public Guide findByQuestionName(String usermessage) {
        LambdaQueryWrapper<Guide> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Guide::getQuestionName, usermessage);
        return  this.getOne(queryWrapper);
    }
}
