package cn.xingxing.mapper;

import cn.xingxing.entity.PredictionModelStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI预测模型统计Mapper
 *
 * @Author: yangyuanliang
 * @Date: 2025-01-15
 * @Version: 1.0
 */
public interface PredictionModelStatsMapper extends BaseMapper<PredictionModelStats> {

    /**
     * 查询模型的最新统计数据
     */
    @Select("SELECT * FROM prediction_model_stats WHERE model_id = #{modelId} " +
            "ORDER BY stats_date DESC LIMIT 1")
    PredictionModelStats selectLatestByModelId(@Param("modelId") String modelId);

    /**
     * 查询模型的最新统计数据（按模型名称）
     */
    @Select("SELECT * FROM prediction_model_stats WHERE model_name = #{modelName} " +
            "ORDER BY stats_date DESC LIMIT 1")
    PredictionModelStats selectLatestByModelName(@Param("modelName") String modelName);

    /**
     * 查询模型的历史统计数据
     */
    @Select("SELECT * FROM prediction_model_stats WHERE model_id = #{modelId} " +
            "AND stats_date >= #{startDate} AND stats_date <= #{endDate} " +
            "ORDER BY stats_date DESC")
    List<PredictionModelStats> selectHistoryByModelId(@Param("modelId") String modelId,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    /**
     * 查询指定类型模型的最新统计
     */
    @Select("SELECT * FROM prediction_model_stats WHERE model_type = #{modelType} " +
            "AND (model_name, stats_date) IN (" +
            "  SELECT model_name, MAX(stats_date) " +
            "  FROM prediction_model_stats " +
            "  WHERE model_type = #{modelType} " +
            "  GROUP BY model_name" +
            ") ORDER BY model_name")
    List<PredictionModelStats> selectLatestByModelType(@Param("modelType") String modelType);
}
