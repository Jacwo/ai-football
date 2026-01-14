package cn.xingxing.service;


import cn.xingxing.entity.HistoricalMatch;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-22
 * @Version: 1.0
 */
public interface HistoricalMatchService {
    List<HistoricalMatch> findHistoricalMatch(String matchId);
}
