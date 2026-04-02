package cn.xingxing.mapper;


import cn.xingxing.entity.AiAnalysisResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-23
 * @Version: 1.0
 */
public interface AiAnalysisResultMapper extends BaseMapper<AiAnalysisResult> {

    /**
     * 物理删除：根据matchId直接删除记录
     */
    @Delete("DELETE FROM ai_analysis_result WHERE match_id = #{matchId}")
    int physicalDeleteByMatchId(@Param("matchId") String matchId);

    /**
     * 查询近N场有结果的预测记录（用于统计）
     */
    @Select("SELECT * FROM ai_analysis_result " +
            "WHERE match_result IS NOT NULL " +
            "AND ai_result IS NOT NULL " +
            "AND match_result REGEXP '^[0-9]+:[0-9]+$' " +
            "ORDER BY match_time DESC " +
            "LIMIT #{limit}")
    List<AiAnalysisResult> selectRecentPredictionsForResult(@Param("limit") int limit);

    /**
     * 查询近N场有比分预测结果的记录（用于统计）
     */
    @Select("SELECT * FROM ai_analysis_result " +
            "WHERE match_result IS NOT NULL " +
            "AND ai_score IS NOT NULL " +
            "ORDER BY match_time DESC " +
            "LIMIT #{limit}")
    List<AiAnalysisResult> selectRecentPredictionsForScore(@Param("limit") int limit);
}
