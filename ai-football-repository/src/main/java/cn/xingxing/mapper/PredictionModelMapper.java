package cn.xingxing.mapper;

import cn.xingxing.entity.PredictionModel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI预测模型Mapper
 *
 * @Author: yangyuanliang
 * @Date: 2025-01-15
 * @Version: 1.0
 */
public interface PredictionModelMapper extends BaseMapper<PredictionModel> {

    /**
     * 查询所有启用的模型
     */
    @Select("SELECT * FROM prediction_model WHERE is_active = 1 ORDER BY model_type, model_name")
    List<PredictionModel> selectActiveModels();

    /**
     * 根据模型类型查询模型
     */
    @Select("SELECT * FROM prediction_model WHERE model_type = #{modelType} AND is_active = 1 ORDER BY model_name")
    List<PredictionModel> selectByModelType(@Param("modelType") String modelType);

    /**
     * 根据模型名称和版本查询模型
     */
    @Select("SELECT * FROM prediction_model WHERE model_name = #{modelName} AND model_version = #{modelVersion}")
    PredictionModel selectByNameAndVersion(@Param("modelName") String modelName, @Param("modelVersion") String modelVersion);
}
