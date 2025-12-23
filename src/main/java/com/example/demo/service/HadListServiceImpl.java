package com.example.demo.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.data.DataService;
import com.example.demo.domain.HadList;
import com.example.demo.mapper.HadListMapperMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        if(hadLists.isEmpty()){
            dataService.loadHadListData(Integer.parseInt(matchId));
            hadLists = hadListMapperMapper.selectList(queryWrapper);
        }


        return hadLists;
    }


}
