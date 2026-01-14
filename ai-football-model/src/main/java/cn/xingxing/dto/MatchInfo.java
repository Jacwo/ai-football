package cn.xingxing.dto;

import cn.xingxing.entity.SubMatchInfo;

import java.util.List;

public class MatchInfo {
    private int matchCount;
    private String matchDate;
    private List<SubMatchInfo> subMatchList;
    private String weekday;

    // Getters and setters
    public int getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(int matchCount) {
        this.matchCount = matchCount;
    }

    public String getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(String matchDate) {
        this.matchDate = matchDate;
    }

    public List<SubMatchInfo> getSubMatchList() {
        return subMatchList;
    }

    public void setSubMatchList(List<SubMatchInfo> subMatchList) {
        this.subMatchList = subMatchList;
    }

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }
}
