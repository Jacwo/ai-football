package cn.xingxing.dto;

import lombok.Data;
import java.util.List;

/**
 * 比赛计算器Value对象
 * @Author: yangyuanliang
 * @Date: 2026-03-24
 * @Version: 1.0
 */
@Data
public class MatchCalculatorValue {
    private List<MatchCalculatorInfo> matchInfoList;
    private List<LeagueInfo> leagueList;
    private Integer totalCount;
    private String lastUpdateTime;
}
