package cn.xingxing.dto;


import lombok.Data;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-23
 * @Version: 1.0
 */
@Data
public class GoalAvg {
    public String awayGoalAvgCnt;        // 客场平均进球数
    public String awayGoalAvgCntRatio;   // 客场进球比率
    public String homeGoalAvgCnt;        // 主场平均进球数
    public String homeGoalAvgCntRatio;   // 主场进球比率
    public int totalLegCnt;              // 总比赛场数
}
