package com.example.demo.service;


import com.example.demo.dto.SubMatchInfo;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-22
 * @Version: 1.0
 */
public interface MatchInfoService {
    List<SubMatchInfo> findCurrentDateMatch();
}
