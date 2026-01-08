package cn.xingxing.dto;

import cn.xingxing.domain.HadList;
import cn.xingxing.domain.HistoricalMatch;
import cn.xingxing.domain.SimilarMatch;
import cn.xingxing.domain.TeamStats;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchAnalysis {
    private String matchId;
    private String homeTeam;
    private String awayTeam;
    private LocalDateTime matchTime;
    private String league;
    private List<SimilarMatch> similarMatches;;
    private List<HistoricalMatch> recentMatches;
    private MatchAnalysisData matchAnalysisData;
    private MatchHistoryData  matchHistoryData;
    private String aiAnalysis;
    private TeamStats homeTeamStats;
    private TeamStats awayTeamStats;
    private List<HadList> hadLists;
    private List<HadList> hhadLists;

    private String information;
    private long timestamp;
}


