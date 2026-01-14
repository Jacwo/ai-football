package cn.xingxing.vo;


import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-24
 * @Version: 1.0
 */
@Data
public class MatchInfoVo {
    @TableId
    private int matchId;
    private String awayTeamAbbName;
    private String awayTeamAllName;
    private String awayTeamId;
    private String backColor;
    private String homeTeamAbbName;
    private String homeTeamAllName;
    private String homeTeamId;
    private String leagueAbbName;
    private String leagueAllName;
    private String leagueId;
    private String matchDate;
    private int matchNum;
    private String matchNumStr;
    private String matchStatus;
    private String matchStatusName;
    private String matchTime;
    private String sectionsNo1;
    private String sectionsNo999;
    //ke
    private String awayWin;
    //ping
    private String draw;
    //zhu
    private String homeWin;
    //让
    private String hhomeWin;
    private String hawayWin;
    private String hdraw;
    private String goalLine;
    private Boolean isSingleMatch =false;
}
