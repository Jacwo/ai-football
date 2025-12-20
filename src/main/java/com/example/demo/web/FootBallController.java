package com.example.demo.web;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.dto.*;
import com.example.demo.dto.url5.Match;
import com.example.demo.dto.url5.MatchInfo5;
import com.example.demo.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.demo.util.HttpClientUtil.METHOD_POST;

@Slf4j
@Controller
@RequestMapping("/api")
public class FootBallController {
    private static String url = "https://webapi.sporttery.cn/gateway/uniform/fb/getMatchDataPageListV1.qry?method=concern";
    private static String url2 = "https://webapi.sporttery.cn/gateway/uniform/football/getFixedBonusV1.qry?clientCode=3001&matchId=";

    private static String url3 = "https://webapi.sporttery.cn/gateway/uniform/football/searchOddsV1.qry?channel=c&type=&single=0&h=%s&a=%s&d=%s";

    private static String url4 = "https://open.feishu.cn/open-apis/bot/v2/hook/a553e701-25e0-4e58-ad26-920fde4c2631";

    // 同主客历史交锋
    private static String url5 = "https://webapi.sporttery.cn/gateway/uniform/football/getResultHistoryV1.qry?" +
            "sportteryMatchId=%s&termLimits=2&tournamentFlag=0&homeAwayFlag=0";

    public static ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Scheduled(initialDelayString = "${schedule.startDelay:10000}",
            fixedDelayString = "${schedule.repeatInterval:14400000}")
    public void load() {
        log.info("定时任务启动----");
        sendMatchInfo("ai");
    }

    @RequestMapping("/match/info/send/{aiInfo}")
    @ResponseBody
    public static String sendMatchInfo(@PathVariable String aiInfo) {
        executorService.execute(() -> send(aiInfo));
        return "success";
    }

    public static void send(String aiInfo) {
        log.info("start send sendMatchInfo...");
        List<Map<String, Object>> result = new ArrayList<>();
        String s = HttpClientUtil.doGet(url, 20000);
        MatchInfoResponse matchInfoResponse = JSONObject.parseObject(s, MatchInfoResponse.class);
        MatchInfoValue value = matchInfoResponse.getValue();
        List<MatchInfo> matchInfoList = value.getMatchInfoList();

        for (MatchInfo matchInfo : matchInfoList) {
            if (checkSkipMatchInfo(matchInfo)) {
                continue;
            }
            List<SubMatchInfo> subMatchList = matchInfo.getSubMatchList();
            subMatchList.forEach(subMatchInfo -> {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("主队", subMatchInfo.getHomeTeamAbbName());
                map1.put("客队", subMatchInfo.getAwayTeamAbbName());
                map1.put("比赛时间:", subMatchInfo.getMatchDate() + " " + subMatchInfo.getMatchTime());
                map1.put("联赛类型：", subMatchInfo.getLeagueAbbName());
                String matchId = subMatchInfo.getMatchId() + "";
                loadRecentMatch(matchId, map1);
                loadHistory(matchId, map1, result);
            });
        }

        if (!CollectionUtils.isEmpty(result)) {
            // 构建消息内容
            String message = buildMessage(result, aiInfo);
            String s1 = HttpClientUtil.doPost(url4, message, 80000);
            log.info("发送消息成功 {}", s1);
        }

    }

    private static Boolean checkSkipMatchInfo(MatchInfo matchInfo) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        try {
            date = dateFormat.parse(matchInfo.getMatchDate() + " 23:59:59");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        // Get the current date
        Date currentDate = new Date();

        // Create a Calendar instance to manipulate dates
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Add two days to the current date
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date currentDatePlusTwoDays = calendar.getTime();
        if (date.compareTo(currentDatePlusTwoDays) > 0) {
            return true;
        }
        return false;
    }

    private static void loadHistory(String matchId, Map<String, Object> map1, List<Map<String, Object>> result) {
        String s1 = HttpClientUtil.doGet(url2 + matchId, 20000);
        MatchInfoResponse2 matchInfoResponse2 = JSONObject.parseObject(s1, MatchInfoResponse2.class);
        MatchInfoValue2 value1 = matchInfoResponse2.getValue();
        OddsHistory oddsHistory = value1.getOddsHistory();
        List<HadList> hhadList = oddsHistory.getHadList();
        List<Map<String, Object>> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(hhadList)) {
            HadList h = oddsHistory.getHadList().get(oddsHistory.getHadList().size() - 1);
            Map<String, Object> map2 = new HashMap<>();
            map2.put("主胜", h.getH());
            map2.put("客胜", h.getA());
            map2.put("平", h.getD());
            String realUrl = String.format(url3, h.getH(), h.getA(), h.getD());
            List<Map<String, Object>> list2 = new ArrayList<>();

            String s2 = HttpClientUtil.doGet(realUrl, 20000);
            MatchInfoResponse3 matchInfoResponse3 = JSONObject.parseObject(s2, MatchInfoResponse3.class);
            MatchInfoValue3 value2 = matchInfoResponse3.getValue();
            List<MatchItem> matchList = value2.getMatchList();
            matchList.forEach(matchItem -> {
                Map<String, Object> map3 = new HashMap<>();
                map3.put("历史主队", matchItem.getHomeTeamAbbName());
                map3.put("历史客队", matchItem.getAwayTeamAbbName());
                map3.put("历史比分主：客", matchItem.getSectionsNo999());
                map3.put("历史联赛类型:", matchItem.getLeaguesAbbName());
                list2.add(map3);
            });
            map2.put("list2", list2);
            list.add(map2);
            map1.put("list", list);
            result.add(map1);
        }else{
            result.add(map1);
        }
    }

    private static void loadRecentMatch(String matchId, Map<String, Object> map1) {
        String realUrl5 = String.format(url5, matchId);
        String url5Response = HttpClientUtil.doGet(realUrl5, 20000);
        Object value3 = JSONObject.parseObject(url5Response).get("value");
        MatchInfo5 matchInfoResponse5 = JSONObject.parseObject(JSONObject.toJSONString(value3), MatchInfo5.class);
        if (matchInfoResponse5 != null) {
            List<Map<String, Object>> list5 = new ArrayList<>();
            List<Match> matchList = matchInfoResponse5.getMatchList();
            if (!CollectionUtils.isEmpty(matchList)) {
                matchList.stream().forEach(match -> {
                    Map<String, Object> map5 = new HashMap<>();
                    map5.put("主队vs客队近期交锋比分", match.getFullCourtGoal());
                    map5.put("主队vs客队近期交锋时间", match.getMatchDate());
                    list5.add(map5);
                });
            }
            map1.put("list5", list5);
        }
    }


    public static String analyzeMatchWithAI(String homeTeam, String awayTeam, String matchData) {
        String url = "https://api.deepseek.com/chat/completions";

        // 专业的系统提示词
        String systemPrompt = "你是一位专业的足球比赛分析师，擅长基于赔率数据、历史交锋记录结果进行综合分析。请用专业、客观的态度进行分析，给出有理有据的比分预测。";

        // 结构化的用户提示词
        String userPrompt = String.format(
                "请对 %s vs %s 这场比赛进行专业分析。\n\n" +
                        "以下是相关数据：\n%s\n\n" +
                        "请从以下维度进行综合分析：\n" +
                        "1. **赔率分析**：解读当前赔率反映的市场预期和胜负概率分布\n" +
                        "2. **基本面分析**：基于历史交锋记录分析两队战术风格、心理优势和近期状态\n" +
                        "3. **进球预期**：结合两队攻防特点预测可能的进球数范围\n" +
                        "请给出：\n" +
                        "2个最可能的比分预测\n" ,
                homeTeam, awayTeam, matchData
        );

        Map<String, Object> data = new HashMap<>();
        data.put("model", "deepseek-chat");
        data.put("stream", false);

        List<Map<String, String>> messages = new ArrayList<>();

        // 系统角色设定
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        // 用户分析请求
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        System.out.println(userPrompt);
        userMsg.put("content", userPrompt);
        messages.add(userMsg);

        data.put("messages", messages);

        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
       //header.put("Authorization", "Bearer sk-9CKemVLLZ3tXORzRzANPBHRFJcPfuBECHuPppVajz7OyS9B1");
        header.put("Authorization", "Bearer sk-2e0ca2fdd7c6405da77d081748210f1e");

        String response = HttpClientUtil.getHttpContent(url, METHOD_POST, JSONObject.toJSONString(data), header, 50000);
        ChatCompletion chatCompletion = JSONObject.parseObject(response, ChatCompletion.class);
        return chatCompletion.getChoices().get(0).getMessage().getContent();
    }


    // 构建包含表格数据的消息内容
    private static String buildMessage(List<Map<String, Object>> result, String aiInfo) {
        LocalDateTime now = LocalDateTime.now();
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 格式化当前时间为字符串
        String formattedDateTime = now.format(formatter);
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("{\n");
        messageBuilder.append("  \"msg_type\": \"interactive\",\n");
        messageBuilder.append("  \"card\": {\n");
        messageBuilder.append("    \"elements\": [\n");
        messageBuilder.append("      {\n");
        messageBuilder.append("        \"tag\": \"div\",\n");
        messageBuilder.append("        \"text\": {\n");
        messageBuilder.append("          \"tag\": \"lark_md\",\n");
        messageBuilder.append("          \"content\": \"带ai分析,请勿频繁点击:\\n https://www.xingxing2019.cn/demo/api/match/info/send/ai\"\n");
        messageBuilder.append("        }\n");
        messageBuilder.append("      },\n");
        messageBuilder.append("      {\n");
        messageBuilder.append("        \"tag\": \"div\",\n");
        messageBuilder.append("        \"text\": {\n");
        messageBuilder.append("          \"tag\": \"lark_md\",\n");
        messageBuilder.append("          \"content\": \"");

        for (int i = 0; i < result.size(); i++) {

            Map<String, Object> re = result.get(i);
            StringBuilder sb = new StringBuilder();
            String leagueType = (String) re.get("联赛类型：");
            String matchTime = (String) re.get("比赛时间:");
            String homeTeam = (String) re.get("主队");
            String awayTeam = (String) re.get("客队");
            sb.append("-----------分隔------------");
            sb.append("\\n当前时间：").append(formattedDateTime);
            sb.append("\\n联赛类型：").append(re.get("联赛类型："));
            sb.append("\\n比赛时间：").append(re.get("比赛时间:"));
            sb.append("\\n主队：").append(re.get("主队")).append(" vs 客队：").append(re.get("客队"));
            List<Map<String, Object>> list5 = (List<Map<String, Object>>) re.get("list5");
            for (int k = 0; k < list5.size(); k++) {
                Map<String, Object> map5 = list5.get(k);
                sb.append("\\n近期交锋时间:").append(map5.get("主队vs客队近期交锋时间"));
                sb.append("\\n近期交锋比分:").append(map5.get("主队vs客队近期交锋比分"));
            }

            List<Map<String, Object>> list = (List<Map<String, Object>>) re.get("list");
            if(!CollectionUtils.isEmpty(list)){
                for (int j = 0; j < list.size(); j++) {
                    Map<String, Object> l = list.get(j);
                    sb.append("\\n最新赔率：").append("主" + l.get("主胜")).append(" 平" + l.get("平")).append(" 客:" + l.get("客胜"));
                    List<Map<String, Object>> list2 = (List<Map<String, Object>>) l.get("list2");
                    if (!CollectionUtils.isEmpty(list2)) {
                        for (Map<String, Object> l2 : list2) {
                            sb.append("\\n历史联赛：").append(l2.get("历史联赛类型:"));
                            sb.append("\\n历史主队: ").append(l2.get("历史主队")).append(" vs 历史客队：").append(l2.get("历史客队")).append(" \\n历史比分：").append(l2.get("历史比分主：客"));
                        }
                    }
                }
            }

            try {
                if ("ai".equals(aiInfo)) {
                    String s = analyzeMatchWithAI(homeTeam, awayTeam, sb.toString());
                    if (s.contains("\n")) {
                        s = s.replaceAll("\\n", " ");
                    }
                    log.info("talkToOpenAi {}", s);
                    sb.append("\\nChatGPT说：").append(s);
                }

            } catch (Exception e) {
                log.error("talk to chat exception {}", e.getMessage());
            }
            messageBuilder.append(sb).append("\\n");
        }

        messageBuilder.append("\"\n");
        messageBuilder.append("        }\n");
        messageBuilder.append("      }\n");
        messageBuilder.append("    ]\n");
        messageBuilder.append("  }\n");
        messageBuilder.append("}");

        return messageBuilder.toString();
    }

    public static void main(String[] args) {
        sendMatchInfo("ai");
    }


    private static boolean isBetween(LocalTime time, LocalTime startTime, LocalTime endTime) {
        if (startTime.isBefore(endTime)) {
            return !time.isBefore(startTime) && time.isBefore(endTime);
        } else {
            // 跨越了一天的情况（例如晚上23点到凌晨9点）
            return !time.isBefore(startTime) || time.isBefore(endTime);
        }
    }


}
