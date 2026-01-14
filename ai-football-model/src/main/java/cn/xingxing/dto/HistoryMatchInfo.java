package cn.xingxing.dto;


import lombok.Data;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-23
 * @Version: 1.0
 */
@Data
public class HistoryMatchInfo {
    public String awayTeamFullCourtGoalCnt;    // 客队全场进球数
    public int awayTeamId;                     // 客队ID
    public String awayTeamShortName;           // 客队简称
    public String fullCourtGoal;               // 全场比分
    public String halfTimeGoal;                // 半场比分
    public String homeMatchResult;             // 主队比赛结果
    public String homeTeamFullCourtGoalCnt;    // 主队全场进球数
    public int homeTeamId;                     // 主队ID
    public String homeTeamShortName;           // 主队简称
    public String matchDate;                   // 比赛日期
    public int matchId;                        // 比赛ID
    public int seasonId;                       // 赛季ID
    public int sportteryAwayTeamId;            // 体彩客队ID
    public int sportteryHomeTeamId;            // 体彩主队ID
    public int sportteryMatchId;               // 体彩比赛ID
    public int sportteryTournamentId;          // 体彩联赛ID
    public String teamMatchResult;             // 球队比赛结果
    public int totalTeamFullCourtGoalCnt;      // 总全场进球数
    public int tournamentId;                   // 联赛ID
    public String tournamentShortName;         // 联赛简称
    public int uniformAwayTeamId;              // 统一客队ID
    public int uniformHomeTeamId;              // 统一主队ID
    public int uniformLeagueId;                // 统一联赛ID
    public String winningTeam;                 // 获胜球队
}
