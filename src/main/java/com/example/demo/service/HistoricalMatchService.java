package com.example.demo.service;


import com.example.demo.domain.HistoricalMatch;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-22
 * @Version: 1.0
 */
public interface HistoricalMatchService {
    List<HistoricalMatch> findHistoricalMatch(String matchId);
}
