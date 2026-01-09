package cn.xingxing.service.impl;


import cn.xingxing.service.HadListService;
import cn.xingxing.service.SimilarMatchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.xingxing.data.DataService;
import cn.xingxing.domain.HadList;
import cn.xingxing.domain.SimilarMatch;
import cn.xingxing.mapper.SimilarMatchMapper;
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
        if (!CollectionUtils.isEmpty(hadList)) {
            LambdaQueryWrapper<SimilarMatch> similarMatchLambdaQueryWrapper = new LambdaQueryWrapper<>();
            similarMatchLambdaQueryWrapper.eq(SimilarMatch::getMatchId, matchId);
            List<SimilarMatch> similarMatches = similarMatchMapper.selectList(similarMatchLambdaQueryWrapper);
          /*  if (CollectionUtils.isEmpty(similarMatches)) {
                dataService.loadSimilarMatchByMatchId(matchId);
                return similarMatchMapper.selectList(similarMatchLambdaQueryWrapper);
            }*/
            return similarMatches;
        }
        return List.of();
    }


}
