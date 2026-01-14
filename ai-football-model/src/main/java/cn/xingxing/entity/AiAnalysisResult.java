package cn.xingxing.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-23
 * @Version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("ai_analysis_result")
public class AiAnalysisResult extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String matchId;
    private String homeTeam;
    private String awayTeam;
    private LocalDateTime matchTime;
    private String homeWin;
    private String draw;
    private String awayWin;
    private String aiAnalysis;
    private String aiScore;
    private String aiResult;
    private String matchResult;
    private String afterMatchAnalysis;
    private LocalDateTime createTime;
}
