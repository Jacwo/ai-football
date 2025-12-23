package com.example.demo;

import cn.xingxing.data.DataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {
	@Autowired
	private DataService dataService;
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
}
