package cn.xingxing.mapper;


import cn.xingxing.entity.AiAnalysisResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

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
}
