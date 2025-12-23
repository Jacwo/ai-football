package cn.xingxing.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("match_info")
public class SubMatchInfo extends BaseEntity {
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
}
