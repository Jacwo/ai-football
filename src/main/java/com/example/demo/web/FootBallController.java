package com.example.demo.web;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.dto.*;
import com.example.demo.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.example.demo.util.HttpClientUtil.METHOD_POST;
@Slf4j
@Controller
@RequestMapping("/api")
public class FootBallController {
    private static String url = "https://webapi.sporttery.cn/gateway/jc/fb/getMatchDataPageListV1.qry?method=concern";
    private static String url2 = "https://webapi.sporttery.cn/gateway/jc/football/getFixedBonusV1.qry?clientCode=3001&matchId=";

    private static String url3 = "https://webapi.sporttery.cn/gateway/jc/football/searchOddsV1.qry?channel=c&type=&single=0&h=%s&a=%s&d=%s";

    private static String url4 = "https://open.feishu.cn/open-apis/bot/v2/hook/a553e701-25e0-4e58-ad26-920fde4c2631";


    @Scheduled(initialDelayString = "${schedule.startDelay:10000}",
            fixedDelayString = "${schedule.repeatInterval:14400000}")
    public void load() {
        log.info("定时任务启动----");
        sendMatchInfo();
    }

    @RequestMapping("/match/info/send")
    @ResponseBody
    public static String sendMatchInfo() {
        List<Map<String, Object>> result = new ArrayList<>();

        String s = HttpClientUtil.doGet(url, 2000);
        MatchInfoResponse matchInfoResponse = JSONObject.parseObject(s, MatchInfoResponse.class);
        MatchInfoValue value = matchInfoResponse.getValue();
        List<MatchInfo> matchInfoList = value.getMatchInfoList();

        for (MatchInfo matchInfo : matchInfoList) {
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
                String s1 = HttpClientUtil.doGet(url2 + matchId, 2000);
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

                    String s2 = HttpClientUtil.doGet(realUrl, 2000);
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
                }
            });
        }
        /**
         * {"msg_type":"text","content":{"text":"request example"}}
         */
        StringBuilder sb = new StringBuilder();
        int flag = 0;
        if (!CollectionUtils.isEmpty(result)) {
            // 构建消息内容
            String message = buildMessage(result);
            String url4 = "https://open.feishu.cn/open-apis/bot/v2/hook/a553e701-25e0-4e58-ad26-920fde4c2631";
            String s1 = HttpClientUtil.doPost(url4, message, 2000);
            System.out.println(s1);
        }


        return "test";
    }


    public static String talkToOpenAi(String msg) {
        String url = "https://api.chatanywhere.tech/v1/chat/completions";
        // 构建请求体
        Map<String, Object> data = new HashMap<>();
        data.put("model", "gpt-3.5-turbo");
        data.put("temperature", 0.7);
        List<Map<String, String>> messages = new ArrayList<>();
        // 添加用户消息
        String message = msg +" 根据最新赔率和历史相同赔率比赛结果,给出推荐比分";
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", message);

        messages.add(userMsg);
        data.put("messages", messages);
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        header.put("Authorization", "Bearer sk-f9UXEOEmJvwT36BuwVQL3CISz2YjCHfj7B7XrI7pk4udq6L0");
        String response = HttpClientUtil.getHttpContent(url, METHOD_POST, JSONObject.toJSONString(data), header, 20000);
        ChatCompletion chatCompletion = JSONObject.parseObject(response, ChatCompletion.class);
        return chatCompletion.getChoices().get(0).getMessage().getContent();
    }

    public static void main(String[] args) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        try {
            date = dateFormat.parse("2024-04-24"+ " 23:59:59");
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
            System.out.println("不");
        }
    }

    // 构建包含表格数据的消息内容
    private static String buildMessage(List<Map<String, Object>> result) {
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
        messageBuilder.append("          \"content\": \"今日分析:\"\n");
        messageBuilder.append("        }\n");
        messageBuilder.append("      },\n");
        messageBuilder.append("      {\n");
        messageBuilder.append("        \"tag\": \"div\",\n");
        messageBuilder.append("        \"text\": {\n");
        messageBuilder.append("          \"tag\": \"lark_md\",\n");
        messageBuilder.append("          \"content\": \"");

        // 构建表格数据
        for (int i = 0; i < result.size(); i++) {

            Map<String, Object> re = result.get(i);
            StringBuilder sb =new StringBuilder();
            sb.append("-----------分隔------------");
            sb.append("\\n当前时间：").append(formattedDateTime);
            sb.append("\\n联赛类型：").append(re.get("联赛类型："));
            sb.append("\\n比赛时间：").append(re.get("比赛时间:"));
            sb.append("\\n主队：").append(re.get("主队")).append(" vs 客队：").append(re.get("客队"));
            List<Map<String, Object>> list = (List<Map<String, Object>>) re.get("list");
            for (int j = 0; j < list.size(); j++) {
                Map<String, Object> l = list.get(j);
                sb.append("\\n最新赔率：").append("主" + l.get("主胜")).append(" 平" + l.get("平")).append(" 客:" + l.get("客胜"));
                List<Map<String, Object>> list2 = (List<Map<String, Object>>) l.get("list2");
                if (!CollectionUtils.isEmpty(list2)) {
                    for (Map<String, Object> l2 : list2) {
                        sb.append("\\n历史联赛：").append(l2.get("历史联赛类型:"));
                        sb.append("\\n主队: ").append(l2.get("历史主队")).append(" vs 客队：").append(l2.get("历史客队")).append(" \\n比分：").append(l2.get("历史比分主：客"));
                    }
                }
            }
            try {
                String s = talkToOpenAi(sb.toString());
                sb.append("\\nChatGPT说：").append(s);
            }catch (Exception e){

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


}
