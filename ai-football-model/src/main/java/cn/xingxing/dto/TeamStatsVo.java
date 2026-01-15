package cn.xingxing.dto;


import cn.xingxing.entity.TeamStats;
import lombok.Builder;
import lombok.Data;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-15
 * @Version: 1.0
 */
@Data
@Builder
public class TeamStatsVo {
    TeamStats home;
    TeamStats away;
    TeamStats all;
}
