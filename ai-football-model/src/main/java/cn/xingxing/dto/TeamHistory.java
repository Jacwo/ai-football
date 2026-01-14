package cn.xingxing.dto;


import lombok.Data;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-23
 * @Version: 1.0
 */
@Data
public class TeamHistory {
    public List<HistoryMatchInfo> matchList;
    public TeamStatistics statistics;
}
