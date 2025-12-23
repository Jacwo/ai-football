package cn.xingxing.service;


import cn.xingxing.domain.SimilarMatch;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-22
 * @Version: 1.0
 */
public interface  SimilarMatchService {
    List<SimilarMatch> findSimilarMatch(String matchId);
}
