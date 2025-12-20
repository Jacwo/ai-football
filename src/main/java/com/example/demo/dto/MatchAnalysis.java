package com.example.demo.dto;

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
    private String homeTeam;
    private String awayTeam;
    private LocalDateTime matchTime;
    private String league;
    private List<OddsInfo> oddsHistory;
    private List<HistoricalMatch> recentMatches;
    private String aiAnalysis;
}


