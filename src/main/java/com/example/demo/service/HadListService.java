package com.example.demo.service;


import com.example.demo.dto.HadList;
import com.example.demo.dto.SimilarMatch;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-22
 * @Version: 1.0
 */
public interface HadListService {
    List<HadList> findHadList(String matchId);
}
