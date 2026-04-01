package cn.xingxing.service;


import cn.xingxing.dto.MatchCalculatorValue;
import cn.xingxing.dto.user.BatchCheckDto;
import cn.xingxing.dto.user.BatchCheckResponseDto;
import cn.xingxing.entity.MatchCalculator;
import cn.xingxing.entity.SubMatchInfo;
import cn.xingxing.vo.MatchInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-22
 * @Version: 1.0
 */
public interface MatchInfoService extends IService<SubMatchInfo> {
    List<MatchInfoVo> findMatchList();

    MatchInfoVo findMatchById(String matchId);

    List<MatchCalculator> getMatchCalculator();

    List<Integer> getUnfinishedMatchIds();
}
