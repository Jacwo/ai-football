package com.example.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-20
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalMatch {
    private String homeTeam;
    private String awayTeam;
    private String score;
    private String league;
    private String matchDate;
}