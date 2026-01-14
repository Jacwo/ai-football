package cn.xingxing.dto;


import lombok.Data;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-23
 * @Version: 1.0
 */
@Data
public class MatchAnalysisData {
    public int awayTeamId;
    public String awayTeamShortName;
    public EachHomeAway eachHomeAway;
    public EachSameHomeAway eachSameHomeAway;
    public GoalAvg goalAvg;
    public int homeTeamId;
    public String homeTeamShortName;
    public Last last;
    public LossGoalAvg lossGoalAvg;
    public int matchId;
    public SameHomeAway sameHomeAway;
    public int sportteryAwayTeamId;
    public int sportteryHomeTeamId;
    public int sportteryMatchId;
    public int uniformAwayTeamId;
    public int uniformHomeTeamId;
}
