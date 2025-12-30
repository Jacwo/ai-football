package cn.xingxing.service.impl;


import cn.xingxing.service.HadListService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.xingxing.data.DataService;
import cn.xingxing.domain.HadList;
import cn.xingxing.mapper.HadListMapperMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-22
 * @Version: 1.0
 */
@Service
public class HadListServiceImpl implements HadListService {

    @Autowired
    private HadListMapperMapper hadListMapperMapper;

    @Autowired
    private DataService dataService;

    @Override
    public List<HadList> findHadList(String matchId) {
        LambdaQueryWrapper<HadList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HadList::getMatchId, matchId);
        List<HadList> hadLists = hadListMapperMapper.selectList(queryWrapper);
        if (hadLists.isEmpty()) {
            dataService.syncHadListByMatchId(matchId);
            hadLists = hadListMapperMapper.selectList(queryWrapper);
        }
        return hadLists;
    }
}
