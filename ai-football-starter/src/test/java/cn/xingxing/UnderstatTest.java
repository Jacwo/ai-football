package cn.xingxing;


import cn.xingxing.data.TeamStatsService;
import cn.xingxing.data.util.EPLDataGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: yangyuanliang
 * @Date: 2026-01-15
 * @Version: 1.0
 */
@SpringBootTest
public class UnderstatTest {
    @Autowired
    private TeamStatsService teamStatsService;

    @Test
    void contextLoads() {
       /* teamStatsService.syncXgData("epl", "2025");
        EPLDataGenerator.saveXgData();
        teamStatsService.loadTeamStats("EPL");
        teamStatsService.loadTeamStatsHome("EPL_home");
        teamStatsService.loadTeamStatsHome("EPL_away");
*/
    }
}
