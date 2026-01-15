package cn.xingxing.data;

import cn.xingxing.entity.TeamStats;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-25
 * @Version: 1.0
 */
public interface TeamStatsService {
    void loadTeamStats(String name);

    TeamStats selectByTeamName(String teamName,String flag);

    TeamStats selectByTeam(String team,String flag);

    void loadTeamStatsHome(String f);

    void loadTeamStatsAway(String f);


    void syncXgData(String league ,String year);
}
