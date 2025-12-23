package com.example.demo.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.data.DataService;
import com.example.demo.domain.SubMatchInfo;
import com.example.demo.mapper.MatchInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-22
 * @Version: 1.0
 */
@Service
public class MatchInfoServiceImpl implements MatchInfoService {
    @Autowired
    private MatchInfoMapper matchInfoMapper;

    @Autowired
    private DataService dataService;


    @Override
    public List<SubMatchInfo> findCurrentDateMatch() {
        LocalDate localDate = LocalDate.now();
        LambdaQueryWrapper<SubMatchInfo> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.between(SubMatchInfo::getMatchDate, localDate, localDate.plusDays(1));

        List<SubMatchInfo> subMatchInfos = matchInfoMapper.selectList(queryWrapper);
        if (subMatchInfos.isEmpty()) {
            dataService.loadMatchInfoData();
            return matchInfoMapper.selectList(queryWrapper);
        }
        return subMatchInfos;
    }
}
