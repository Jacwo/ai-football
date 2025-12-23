package cn.xingxing.dto;

import java.util.List;

public class MatchItem {
    private int matchId;
    private int leagueId;
    private String leaguesAllName;
    private String leaguesAbbName;
    private int homeTeamId;
    private String homeTeamAbbName;
    private String homeTeamAllName;
    private int awayTeamId;
    private String awayTeamAllName;
    private String awayTeamAbbName;
    private String sectionsNo999;
    private String backColor;
    private int single;
    private List<OddsItem> odds;

    // Getter and Setter methods
    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public int getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(int leagueId) {
        this.leagueId = leagueId;
    }

    public String getLeaguesAllName() {
        return leaguesAllName;
    }

    public void setLeaguesAllName(String leaguesAllName) {
        this.leaguesAllName = leaguesAllName;
    }

    public String getLeaguesAbbName() {
        return leaguesAbbName;
    }

    public void setLeaguesAbbName(String leaguesAbbName) {
        this.leaguesAbbName = leaguesAbbName;
    }

    public int getHomeTeamId() {
        return homeTeamId;
    }

    public void setHomeTeamId(int homeTeamId) {
        this.homeTeamId = homeTeamId;
    }

    public String getHomeTeamAbbName() {
        return homeTeamAbbName;
    }

    public void setHomeTeamAbbName(String homeTeamAbbName) {
        this.homeTeamAbbName = homeTeamAbbName;
    }

    public String getHomeTeamAllName() {
        return homeTeamAllName;
    }

    public void setHomeTeamAllName(String homeTeamAllName) {
        this.homeTeamAllName = homeTeamAllName;
    }

    public int getAwayTeamId() {
        return awayTeamId;
    }

    public void setAwayTeamId(int awayTeamId) {
        this.awayTeamId = awayTeamId;
    }

    public String getAwayTeamAllName() {
        return awayTeamAllName;
    }

    public void setAwayTeamAllName(String awayTeamAllName) {
        this.awayTeamAllName = awayTeamAllName;
    }

    public String getAwayTeamAbbName() {
        return awayTeamAbbName;
    }

    public void setAwayTeamAbbName(String awayTeamAbbName) {
        this.awayTeamAbbName = awayTeamAbbName;
    }

    public String getSectionsNo999() {
        return sectionsNo999;
    }

    public void setSectionsNo999(String sectionsNo999) {
        this.sectionsNo999 = sectionsNo999;
    }

    public String getBackColor() {
        return backColor;
    }

    public void setBackColor(String backColor) {
        this.backColor = backColor;
    }

    public int getSingle() {
        return single;
    }

    public void setSingle(int single) {
        this.single = single;
    }

    public List<OddsItem> getOdds() {
        return odds;
    }

    public void setOdds(List<OddsItem> odds) {
        this.odds = odds;
    }
}
