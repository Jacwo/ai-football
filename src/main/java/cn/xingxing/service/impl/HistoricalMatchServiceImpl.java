package cn.xingxing.service.impl;


import cn.xingxing.service.HistoricalMatchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.xingxing.data.DataService;
import cn.xingxing.domain.HistoricalMatch;
import cn.xingxing.mapper.HistoricalMatchMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-22
 * @Version: 1.0
 */
@Service
public class HistoricalMatchServiceImpl implements HistoricalMatchService {
    @Autowired
    private HistoricalMatchMapper historicalMatchMapper;
    @Autowired
    private DataService dataService;

    @Override
    public List<HistoricalMatch> findHistoricalMatch(String matchId) {
        LambdaQueryWrapper<HistoricalMatch> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HistoricalMatch::getMatchId, matchId);
        List<HistoricalMatch> historicalMatches = historicalMatchMapper.selectList(queryWrapper);
        if (historicalMatches.isEmpty()) {
            dataService.loadHistoryDataByMatchId(matchId);
            return historicalMatchMapper.selectList(queryWrapper);
        }
        return historicalMatches;
    }
}
