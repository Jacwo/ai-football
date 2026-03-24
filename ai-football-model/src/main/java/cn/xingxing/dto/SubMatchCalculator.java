package cn.xingxing.dto;

import lombok.Data;

/**
 * 子比赛计算器数据
 * @Author: yangyuanliang
 * @Date: 2026-03-24
 * @Version: 1.0
 */
@Data
public class SubMatchCalculator {
    private Integer matchId;
    private Integer matchNum;
    private String matchNumStr;
    private String matchNumDate;
    private String matchDate;
    private String matchTime;
    private String matchWeek;
    private Integer homeTeamId;
    private String homeTeamAbbName;
    private String homeTeamAbbEnName;
    private String homeTeamAllName;
    private String homeTeamCode;
    private String homeRank;
    private Integer awayTeamId;
    private String awayTeamAbbName;
    private String awayTeamAbbEnName;
    private String awayTeamAllName;
    private String awayTeamCode;
    private String awayRank;
    private Integer leagueId;
    private String leagueAbbName;
    private String leagueAllName;
    private String leagueCode;
    private String matchStatus;
    private Integer sellStatus;
    private String backColor;
    private String businessDate;
    private Integer isHide;
    private Integer isHot;
    private String taxDateNo;
    private Integer bettingSingle;
    private Integer bettingAllUp;
    private Integer baseHomeTeamId;
    private Integer baseAwayTeamId;
    private String groupName;
    private String lineNum;
    private String matchName;
    private String remark;

    // 赔率数据对象
    private HadOdds had;
    private HhadOdds hhad;
    private TtgOdds ttg;
    private HafuOdds hafu;
    private CrsOdds crs;
}
