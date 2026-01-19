package cn.xingxing.service;


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
}
