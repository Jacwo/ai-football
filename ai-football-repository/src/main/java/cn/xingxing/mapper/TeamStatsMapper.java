package cn.xingxing.mapper;


import cn.xingxing.entity.TeamStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-22
 * @Version: 1.0
 */
public interface TeamStatsMapper extends BaseMapper<TeamStats> {
    int insertBatch(List<TeamStats> teamStatsList);
}