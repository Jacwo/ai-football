package com.example.demo.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.data.DataService;
import com.example.demo.domain.HadList;
import com.example.demo.domain.SimilarMatch;
import com.example.demo.mapper.SimilarMatchMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-22
 * @Version: 1.0
 */
@Service
public class SimilarMatchServiceImpl implements SimilarMatchService {

    @Autowired
    private HadListService hadListService;
    @Autowired
    private DataService dataService;

    @Autowired
    private SimilarMatchMapper similarMatchMapper;

    @Override
    public List<SimilarMatch> findSimilarMatch(String matchId) {
        List<HadList> hadList = hadListService.findHadList(matchId);
        if(!CollectionUtils.isEmpty(hadList)){
            LambdaQueryWrapper<SimilarMatch> similarMatchLambdaQueryWrapper = new LambdaQueryWrapper<>();
            similarMatchLambdaQueryWrapper.eq(SimilarMatch::getMatchId, matchId);
            List<SimilarMatch> similarMatches = similarMatchMapper.selectList(similarMatchLambdaQueryWrapper);
            if(!CollectionUtils.isEmpty(similarMatches)){
                dataService.loadSimilarMatch(Integer.parseInt(matchId));
                return similarMatchMapper.selectList(similarMatchLambdaQueryWrapper);
            }
            return similarMatches;
        }
        return List.of();
    }


}
