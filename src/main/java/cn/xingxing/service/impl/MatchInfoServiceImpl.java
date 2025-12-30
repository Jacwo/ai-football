package cn.xingxing.service.impl;


import cn.xingxing.service.MatchInfoService;
import cn.xingxing.vo.MatchInfoVo;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.xingxing.data.DataService;
import cn.xingxing.domain.SubMatchInfo;
import cn.xingxing.mapper.MatchInfoMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-22
 * @Version: 1.0
 */
@Slf4j
@Service
public class MatchInfoServiceImpl extends ServiceImpl<MatchInfoMapper,SubMatchInfo> implements MatchInfoService {
    @Autowired
    private MatchInfoMapper matchInfoMapper;

    @Autowired
    private DataService dataService;


    @Override
    public List<SubMatchInfo> findCurrentDateMatch() {
       // dataService.loadMatchInfoData();
        LocalDate localDate = LocalDate.now();
        LambdaQueryWrapper<SubMatchInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(SubMatchInfo::getMatchDate, localDate, localDate.plusDays(1));
        List<SubMatchInfo> subMatchInfos = matchInfoMapper.selectList(queryWrapper);
        return subMatchInfos.stream()
                .filter(subMatchInfo -> {
                    // 构建比赛时间的LocalDateTime
                    String matchDate = subMatchInfo.getMatchDate();
                    String matchTime = subMatchInfo.getMatchTime();
                    try {
                        // 拼接日期时间字符串，并转换为LocalDateTime
                        String dateTimeStr = matchDate + " " + matchTime;
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        LocalDateTime matchDateTime = LocalDateTime.parse(dateTimeStr, formatter);
                        // 返回比赛时间大于当前时间的记录
                        return matchDateTime.isAfter(LocalDateTime.now());
                    } catch (DateTimeParseException e) {
                        log.error("时间格式解析失败: date={}, time={}", matchDate, matchTime, e);
                        return false; // 格式错误，跳过
                    }
                }).collect(Collectors.toList());
    }

    @Override
    public SubMatchInfo findMatchById(String matchId) {
        LambdaQueryWrapper<SubMatchInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SubMatchInfo::getMatchId, matchId);
        return matchInfoMapper.selectOne(queryWrapper);
    }
}
