package cn.xingxing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "football.api")
public class FootballApiConfig {

    // 彩票API
    private String matchListUrl = "https://webapi.sporttery.cn/gateway/uniform/fb/getMatchDataPageListV1.qry?method=concern";
    private String fixedBonusUrl = "https://webapi.sporttery.cn/gateway/uniform/football/getFixedBonusV1.qry?clientCode=3001&matchId=";
    private String searchOddsUrl = "https://webapi.sporttery.cn/gateway/uniform/football/searchOddsV1.qry?channel=c&type=&single=0&h=%s&a=%s&d=%s";
    private String searchOddsUrl2 = "https://webapi.sporttery.cn/gateway/uniform/football/searchOddsV1.qry?channel=c&type=&single=1&h=%s&a=%s&d=%s";

    private String resultHistoryUrl = "https://webapi.sporttery.cn/gateway/uniform/football/getResultHistoryV1.qry?" +
            "sportteryMatchId=%s&termLimits=10&tournamentFlag=0&homeAwayFlag=0";
    //比赛近况
    private String matchHistoryUrl =
            "https://webapi.sporttery.cn/gateway/uniform/football/getMatchResultV1.qry?sportteryMatchId=%s&termLimits=5&tournamentFlag=0&homeAwayFlag=0";
    //比赛结果
    private String matchResultUrl = "https://webapi.sporttery.cn/gateway/uniform/fb/getMatchDataPageListV1.qry?" +
            "method=result&pageNo=1&pageType=0&pageSize=300";
    //比赛特征
    private String matchFeatureUrl= "https://webapi.sporttery.cn/gateway/uniform/football/getMatchFeatureV1.qry?termLimits=10&sportteryMatchId=%s";
    //射手榜
    private String player = "https://webapi.sporttery.cn/gateway/uniform/football/getMatchPlayerV1.qry?sportteryMatchId=2036289&termLimits=3";
    //伤停信息
    private String injurySuspension = "https://webapi.sporttery.cn/gateway/uniform/football/getInjurySuspensionV1.qry?sportteryMatchId=2036289";
    // 飞书Webhook
    private String feishuWebhookUrl = "https://open.feishu.cn/open-apis/bot/v2/hook/a553e701-25e0-4e58-ad26-920fde4c2631";

    // 定时任务配置
    private long scheduleInitialDelay = 10000;
    private long scheduleFixedDelay = 14400000;

    // 超时配置
    private int httpConnectTimeout = 20000;
    private int httpReadTimeout = 50000;
}