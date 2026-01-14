package cn.xingxing.dto;

import java.util.List;

public class MatchInfoValue3 {
    private int winNum;
    private String a;
    private String d;
    private int drawNum;
    private String h;
    private int loseNum;
    private List<MatchItem> matchList;

    // Getter and Setter methods
    public int getWinNum() {
        return winNum;
    }

    public void setWinNum(int winNum) {
        this.winNum = winNum;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public int getDrawNum() {
        return drawNum;
    }

    public void setDrawNum(int drawNum) {
        this.drawNum = drawNum;
    }

    public String getH() {
        return h;
    }

    public void setH(String h) {
        this.h = h;
    }

    public int getLoseNum() {
        return loseNum;
    }

    public void setLoseNum(int loseNum) {
        this.loseNum = loseNum;
    }

    public List<MatchItem> getMatchList() {
        return matchList;
    }

    public void setMatchList(List<MatchItem> matchList) {
        this.matchList = matchList;
    }
}
