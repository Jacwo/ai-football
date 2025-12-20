package com.example.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-20
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OddsInfo {
    private Double homeWin;
    private Double draw;
    private Double awayWin;
    private List<HistoricalMatch> similarMatches;
}
