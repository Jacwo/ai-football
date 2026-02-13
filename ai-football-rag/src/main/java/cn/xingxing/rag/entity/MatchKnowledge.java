package cn.xingxing.rag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 比赛知识库实体 - 存储历史预测的学习经验
 */
@Data
@TableName("match_knowledge")
public class MatchKnowledge {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 关联的比赛ID
     */
    private String matchId;

    /**
     * 联赛名称
     */
    private String league;

    /**
     * 主队名称
     */
    private String homeTeam;

    /**
     * 客队名称
     */
    private String awayTeam;

    /**
     * 比赛时间
     */
    private LocalDateTime matchTime;

    /**
     * 赛前赔率快照 (JSON格式: {"h": 1.5, "d": 3.5, "a": 5.0})
     */
    private String oddsSnapshot;

    /**
     * AI预测结果 (主胜/平局/客胜)
     */
    private String aiPrediction;

    /**
     * AI预测比分
     */
    private String aiScore;

    /**
     * AI预测置信度 (0-100)
     */
    private Integer confidence;

    /**
     * 实际比赛结果
     */
    private String actualResult;

    /**
     * 实际比分
     */
    private String actualScore;

    /**
     * 预测是否正确 (胜负预测)
     */
    private Boolean predictionCorrect;

    /**
     * 比分预测是否正确
     */
    private Boolean scoreCorrect;

    /**
     * 关键分析特征 (JSON格式)
     * 包含: xG差值、赔率变化幅度、历史交锋胜率等
     */
    private String keyFeatures;

    /**
     * 分析摘要 - 用于向量检索
     */
    private String analysisSummary;

    /**
     * 学习经验 - 从赛后复盘中提取
     */
    private String learningInsight;

    /**
     * 场景标签 (如: 强队对弱队、德比战、保级战等)
     */
    private String scenarioTags;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
