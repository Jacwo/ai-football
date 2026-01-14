package cn.xingxing.vo;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-24
 * @Version: 1.0
 */
@Data
public class AiAnalysisResultVo {
    private String id;
    private String matchId;
    private String homeTeam;
    private String awayTeam;
    private LocalDateTime matchTime;
    private String homeWin;
    private String draw;
    private String awayWin;
    private String aiAnalysis;
    private String matchResult;
    private String aiScore;
    private String aiResult;
    private String afterMatchAnalysis;
    private LocalDateTime createTime;

}
