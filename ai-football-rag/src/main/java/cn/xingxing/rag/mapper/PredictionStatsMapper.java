package cn.xingxing.rag.mapper;

import cn.xingxing.rag.entity.PredictionStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预测统计 Mapper
 */
@Mapper
public interface PredictionStatsMapper extends BaseMapper<PredictionStats> {

}
