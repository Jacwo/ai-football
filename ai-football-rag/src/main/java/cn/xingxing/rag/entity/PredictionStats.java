package cn.xingxing.rag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预测统计实体 - 追踪AI预测准确率
 */
@Data
@TableName("prediction_stats")
public class PredictionStats {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 统计日期
     */
    private LocalDate statsDate;

    /**
     * 联赛名称 (null表示全部联赛)
     */
    private String league;

    /**
     * 总预测场次
     */
    private Integer totalPredictions;

    /**
     * 胜负预测正确数
     */
    private Integer correctPredictions;

    /**
     * 比分预测正确数
     */
    private Integer correctScores;

    /**
     * 高置信度预测数 (置信度>=70)
     */
    private Integer highConfidencePredictions;

    /**
     * 高置信度正确数
     */
    private Integer highConfidenceCorrect;

    /**
     * 冷门预测成功数
     */
    private Integer upsetCorrect;

    /**
     * 冷门预测总数
     */
    private Integer upsetTotal;

    /**
     * 胜负预测准确率
     */
    private BigDecimal resultAccuracy;

    /**
     * 比分预测准确率
     */
    private BigDecimal scoreAccuracy;

    /**
     * 高置信度准确率
     */
    private BigDecimal highConfidenceAccuracy;

    /**
     * 冷门预测准确率
     */
    private BigDecimal upsetAccuracy;

    /**
     * ROI (投资回报率模拟)
     */
    private BigDecimal roi;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
