package cn.xingxing.dto;


import lombok.Data;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-23
 * @Version: 1.0
 */
@Data
public class EachHomeAway {
    public int awayDrawMatchCnt;      // 客场平局比赛数
    public int awayLossGoalMatchCnt;  // 客场输球比赛数
    public String awayScoreRatio;     // 客场得分比率
    public int awayWinGoalMatchCnt;   // 客场赢球比赛数
    public int homeDrawMatchCnt;      // 主场平局比赛数
    public int homeLossGoalMatchCnt;  // 主场输球比赛数
    public String homeScoreRatio;     // 主场得分比率
    public int homeWinGoalMatchCnt;   // 主场赢球比赛数
    public int totalLegCnt;          // 总比赛场数
}
