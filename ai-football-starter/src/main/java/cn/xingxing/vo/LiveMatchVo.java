package cn.xingxing.vo;

import lombok.Data;
import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2026-04-01
 * @Version: 1.0
 */
@Data
public class LiveMatchVo {
    private Integer matchId;
    private String awayTeamAbbName;
    private String awayTeamAllName;
    private String homeTeamAbbName;
    private String homeTeamAllName;
    private String leagueAbbName;
    private String leagueAllName;
    private String leagueId;
    private String matchDate;
    private Integer matchNum;
    private String matchNumStr;
    private String matchStatus;
    private String matchStatusName;
    private String matchTime;
    private String matchMinute;
    private String matchMinuteExtra;
    private String matchPhaseTc;
    private String matchPhaseTcName;
    private String sectionsNo1;
    private String sectionsNo999;
    private String backColor;
    private List<MatchEvent> eventList;

    @Data
    public static class MatchEvent {
        private String eventCode;
        private String eventTc;
        private String matchMinute;
        private String matchMinuteExtra;
        private String matchPhaseTc;
        private String personEnName;
        private String teamType;
        private String uniformNo;
    }
}
