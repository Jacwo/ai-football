package cn.xingxing.service.impl;


import cn.xingxing.service.MatchInfoService;
import cn.xingxing.vo.MatchInfoVo;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.xingxing.data.DataService;
import cn.xingxing.entity.SubMatchInfo;
import cn.xingxing.mapper.MatchInfoMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-22
 * @Version: 1.0
 */
@Slf4j
@Service
public class MatchInfoServiceImpl extends ServiceImpl<MatchInfoMapper, SubMatchInfo> implements MatchInfoService {
    @Autowired
    private MatchInfoMapper matchInfoMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public List<SubMatchInfo> findMatchList() {
        LambdaQueryWrapper<SubMatchInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SubMatchInfo::getMatchStatus, "2")
                .or(wq -> wq.eq(SubMatchInfo::getMatchStatus, "3")
                        .eq(SubMatchInfo::getMatchStatusName, "暂停销售"));
        List<SubMatchInfo> subMatchInfos = matchInfoMapper.selectList(queryWrapper);
        return subMatchInfos.stream().filter(f -> {
            LocalDateTime localDateTime = parseMatchTime(f.getMatchDate(), f.getMatchTime());
            return localDateTime.isAfter(LocalDateTime.now());
        }).toList();
    }

    @Override
    public SubMatchInfo findMatchById(String matchId) {
        LambdaQueryWrapper<SubMatchInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SubMatchInfo::getMatchId, matchId);
        return matchInfoMapper.selectOne(queryWrapper);
    }


    private LocalDateTime parseMatchTime(String date, String time) {
        try {
            String dateTimeStr = date + " " + time;
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("解析比赛时间失败: {} {}", date, time, e);
            return LocalDateTime.now();
        }
    }

}
