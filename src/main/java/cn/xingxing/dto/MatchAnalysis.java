package cn.xingxing.dto;

import cn.xingxing.domain.HistoricalMatch;
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
    private List<OddsInfo> oddsHistory;
    private List<HistoricalMatch> recentMatches;
    private String aiAnalysis;
}


