package cn.xingxing.dto.url5;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Statistics {
    private int drawMatchCnt;
    private String drawProbability;
    private int goalCnt;
    private int lossGoalCnt;
    private int lossGoalMatchCnt;
    private String lossProbability;
    private int netGoal;
    private int sportteryTeamId;
    private int teamId;
    private String teamShortName;
    private int totalLegCnt;
    private int winGoalMatchCnt;
    private String winProbability;
}
