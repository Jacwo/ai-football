package cn.xingxing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI预测模型准确率统计实体类
 *
 * @Author: yangyuanliang
 * @Date: 2025-01-15
 * @Version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("prediction_model_stats")
public class PredictionModelStats extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型类型 (result:胜负预测, score:比分预测)
     */
    private String modelType;

    /**
     * 统计时间
     */
    private LocalDateTime statsDate;

    /**
     * 样本数量 (默认近100场)
     */
    private Integer sampleSize;

    /**
     * 总预测场次
     */
    private Integer totalPredictions;

    /**
     * 正确预测数
     */
    private Integer correctPredictions;

    /**
     * 准确率 (%)
     */
    private BigDecimal accuracyRate;

    /**
     * 主胜预测数
     */
    private Integer homeWinCount;

    /**
     * 主胜预测正确数
     */
    private Integer homeWinCorrect;

    /**
     * 平局预测数
     */
    private Integer drawCount;

    /**
     * 平局预测正确数
     */
    private Integer drawCorrect;

    /**
     * 客胜预测数
     */
    private Integer awayWinCount;

    /**
     * 客胜预测正确数
     */
    private Integer awayWinCorrect;

    /**
     * 近10场准确率
     */
    private BigDecimal recent10Accuracy;

    /**
     * 近20场准确率
     */
    private BigDecimal recent20Accuracy;

    /**
     * 近50场准确率
     */
    private BigDecimal recent50Accuracy;

    /**
     * 分联赛统计 JSON格式
     */
    private String leagueStats;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
