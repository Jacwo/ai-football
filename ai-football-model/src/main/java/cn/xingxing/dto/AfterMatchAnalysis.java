package cn.xingxing.dto;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-23
 * @Version: 1.0
 */
@Data
public class AfterMatchAnalysis {
    private String aiAnalysis;
    private String matchResult;
    private String homeTeam;
    private String awayTeam;
    private LocalDateTime matchTime;
}
