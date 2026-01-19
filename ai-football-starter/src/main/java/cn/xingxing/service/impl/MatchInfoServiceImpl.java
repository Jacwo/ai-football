package cn.xingxing.service.impl;


import cn.xingxing.data.TeamStatsService;
import cn.xingxing.entity.TeamStats;
import cn.xingxing.service.MatchInfoService;
import cn.xingxing.vo.MatchInfoVo;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.xingxing.data.DataService;
import cn.xingxing.entity.SubMatchInfo;
import cn.xingxing.mapper.MatchInfoMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

    @Autowired
    private TeamStatsService teamStatsService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public List<MatchInfoVo> findMatchList() {
        LambdaQueryWrapper<SubMatchInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SubMatchInfo::getMatchStatus, "2")
                .or(wq -> wq.eq(SubMatchInfo::getMatchStatus, "3")
                        .eq(SubMatchInfo::getMatchStatusName, "暂停销售"));
        List<SubMatchInfo> subMatchInfos = matchInfoMapper.selectList(queryWrapper);
        List<SubMatchInfo> list = subMatchInfos.stream().filter(f -> {
            LocalDateTime localDateTime = parseMatchTime(f.getMatchDate(), f.getMatchTime());
            return localDateTime.isAfter(LocalDateTime.now());
        }).toList();
        List<MatchInfoVo> matchInfoVos = JSONObject.parseArray(JSONObject.toJSONString(list), MatchInfoVo.class);
        matchInfoVos.forEach(m->{
            TeamStats homeStats = teamStatsService.selectByTeam(m.getHomeTeamAbbName(), "all");
            TeamStats awayStats = teamStatsService.selectByTeam(m.getAwayTeamAbbName(), "all");
            if(homeStats!=null && awayStats!=null){
                m.setHomeTeamRank(homeStats.getRankNum());
                m.setAwayTeamRank(awayStats.getRankNum());
            }
        });
        return  matchInfoVos;
    }

    @Override
    public MatchInfoVo findMatchById(String matchId) {
        MatchInfoVo matchInfoVo = new MatchInfoVo();
        LambdaQueryWrapper<SubMatchInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SubMatchInfo::getMatchId, matchId);
        SubMatchInfo subMatchInfo = matchInfoMapper.selectOne(queryWrapper);
        BeanUtils.copyProperties(subMatchInfo, matchInfoVo);
        TeamStats homeStats = teamStatsService.selectByTeam(matchInfoVo.getHomeTeamAbbName(), "all");
        TeamStats awayStats = teamStatsService.selectByTeam(matchInfoVo.getAwayTeamAbbName(), "all");
        if(homeStats!=null && awayStats!=null){
            matchInfoVo.setHomeTeamRank(homeStats.getRankNum());
            matchInfoVo.setAwayTeamRank(awayStats.getRankNum());
        }
        return matchInfoVo;
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
