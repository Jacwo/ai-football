package cn.xingxing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * AI预测模型实体类
 *
 * @Author: yangyuanliang
 * @Date: 2025-01-15
 * @Version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("prediction_model")
public class PredictionModel extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型类型 (result:胜负预测, score:比分预测)
     */
    private String modelType;

    /**
     * 模型版本
     */
    private String modelVersion;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 是否启用 (1:启用, 0:禁用)
     */
    private Integer isActive;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
