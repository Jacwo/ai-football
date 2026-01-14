package cn.xingxing.dto;


import lombok.Data;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-23
 * @Version: 1.0
 */
@Data
public class SameHomeAway {
    public int awayDrawMatchCnt;
    public int awayLossGoalMatchCnt;
    public String awayScoreRatio;
    public int awayWinGoalMatchCnt;
    public int homeDrawMatchCnt;
    public int homeLossGoalMatchCnt;
    public String homeScoreRatio;
    public int homeWinGoalMatchCnt;
    public int totalLegCnt;
}
