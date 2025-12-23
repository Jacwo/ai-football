package com.example.demo.dto;

import com.example.demo.domain.HadList;

import java.util.List;

public class OddsHistory {
    private String awayTeamAllName;
    private String awayTeamAbbName;
    private String homeTeamAllName;
    private List<CrsList> crsList;
    private String homeTeamAbbName;
    private List<HadList> hadList;
    //让球
    private List<HadList> hhadList;

    private List<TtgList> ttgList;
    private String leagueAbbName;
    private String leagueId;
    private String leagueAllName;
    private List<SingleList> singleList;
    private int homeTeamId;
    private int awayTeamId;
    private int matchId;
    private List<HafuList> hafuList;

    public void setHadList(List<HadList> hadList) {
        this.hadList = hadList;
    }

    // Getter and Setter methods
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

    public String getHomeTeamAllName() {
        return homeTeamAllName;
    }

    public void setHomeTeamAllName(String homeTeamAllName) {
        this.homeTeamAllName = homeTeamAllName;
    }

    public List<CrsList> getCrsList() {
        return crsList;
    }

    public void setCrsList(List<CrsList> crsList) {
        this.crsList = crsList;
    }

    public String getHomeTeamAbbName() {
        return homeTeamAbbName;
    }

    public void setHomeTeamAbbName(String homeTeamAbbName) {
        this.homeTeamAbbName = homeTeamAbbName;
    }

    public List<HadList> getHadList() {
        return hadList;
    }


    public List<HadList> getHhadList() {
        return hhadList;
    }

    public void setHhadList(List<HadList> hhadList) {
        this.hhadList = hhadList;
    }

    public List<TtgList> getTtgList() {
        return ttgList;
    }

    public void setTtgList(List<TtgList> ttgList) {
        this.ttgList = ttgList;
    }

    public String getLeagueAbbName() {
        return leagueAbbName;
    }

    public void setLeagueAbbName(String leagueAbbName) {
        this.leagueAbbName = leagueAbbName;
    }

    public String getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(String leagueId) {
        this.leagueId = leagueId;
    }

    public String getLeagueAllName() {
        return leagueAllName;
    }

    public void setLeagueAllName(String leagueAllName) {
        this.leagueAllName = leagueAllName;
    }

    public List<SingleList> getSingleList() {
        return singleList;
    }

    public void setSingleList(List<SingleList> singleList) {
        this.singleList = singleList;
    }

    public int getHomeTeamId() {
        return homeTeamId;
    }

    public void setHomeTeamId(int homeTeamId) {
        this.homeTeamId = homeTeamId;
    }

    public int getAwayTeamId() {
        return awayTeamId;
    }

    public void setAwayTeamId(int awayTeamId) {
        this.awayTeamId = awayTeamId;
    }

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public List<HafuList> getHafuList() {
        return hafuList;
    }

    public void setHafuList(List<HafuList> hafuList) {
        this.hafuList = hafuList;
    }
}
