package cn.xingxing.service;


import cn.xingxing.entity.HadList;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-22
 * @Version: 1.0
 */
public interface HadListService {
    List<HadList> findHadList(String matchId);

    List<HadList>  findHHadList(String matchId);
}
