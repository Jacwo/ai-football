package cn.xingxing.dto;


import lombok.Data;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-23
 * @Version: 1.0
 */
@Data
public class TeamStatistics {
    public int drawMatchCnt;          // 平局比赛数
    public String drawProbability;    // 平局概率
    public int goalCnt;               // 总进球数
    public int lossGoalCnt;           // 失球数
    public int lossGoalMatchCnt;      // 输球比赛数
    public String lossProbability;    // 输球概率
    public int netGoal;               // 净胜球
    public int sportteryTeamId;       // 体彩球队ID
    public int teamId;                // 球队ID
    public String teamShortName;      // 球队简称
    public int totalLegCnt;           // 总比赛场数
    public int uniformTeamId;         // 统一球队ID
    public int winGoalMatchCnt;       // 赢球比赛数
    public String winProbability;     // 赢球概率
}
