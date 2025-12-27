package cn.xingxing;

import cn.xingxing.data.DataService;
import cn.xingxing.domain.TeamStats;
import cn.xingxing.dto.MatchAnalysis;
import cn.xingxing.notify.NotifyService;
import cn.xingxing.service.FootballAnalysisService;
import cn.xingxing.service.TeamStatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class DemoApplicationTests {
	@Autowired
	private DataService dataService;

	@Autowired
	private TeamStatsService teamStatsService;

	@Autowired
	private FootballAnalysisService analysisService;

	@Autowired
	private NotifyService notifyService;
	@Test
	void contextLoads() {
		dataService.loadMatchInfoData();
	}


	@Test
	void contextLoads2() {
		dataService.loadHistoryData(0);
	}


	@Test
	void contextLoads3() {
		dataService.loadHadListData(0);
	}


	@Test
	void contextLoads4() {
		dataService.loadSimilarMatch(0);
	}


	@Test
	void contextLoads5() {
		dataService.loadMatchResult(0);
	}


	@Test
	void contextLoads6() {
		dataService.afterMatchAnalysis(0);
	}


	@Test
	void contextLoads7() {
		dataService.loadTeamStats();
	}

	@Test
	void contextLoads8() {
		TeamStats teamStats = teamStatsService.selectByTeamName("阿森纳");
		System.out.println(teamStats);
	}


	@Test
	void contextLoads9() {
		MatchAnalysis matchAnalysis = analysisService.analysisByMatchId("2036380");
		System.out.println(matchAnalysis.getAiAnalysis());
		notifyService.sendMsg(List.of(matchAnalysis));
	}
}
