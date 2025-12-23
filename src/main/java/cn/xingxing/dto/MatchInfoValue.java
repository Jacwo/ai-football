package cn.xingxing.dto;

import java.util.List;

public class MatchInfoValue {
    private List<MatchInfo> matchInfoList;
    private int nextPage;
    private int pageNo;
    private int prePage;
    private String tagMatchId;

    // Getters and setters
    public List<MatchInfo> getMatchInfoList() {
        return matchInfoList;
    }

    public void setMatchInfoList(List<MatchInfo> matchInfoList) {
        this.matchInfoList = matchInfoList;
    }

    public int getNextPage() {
        return nextPage;
    }

    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPrePage() {
        return prePage;
    }

    public void setPrePage(int prePage) {
        this.prePage = prePage;
    }

    public String getTagMatchId() {
        return tagMatchId;
    }

    public void setTagMatchId(String tagMatchId) {
        this.tagMatchId = tagMatchId;
    }
}
