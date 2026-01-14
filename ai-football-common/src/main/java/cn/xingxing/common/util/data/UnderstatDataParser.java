package cn.xingxing.common.util.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UnderstatDataParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 智能JSON解析器，处理各种编码格式
     */
    public static JsonNode parseJavaScriptData(String rawScriptContent) {
        try {
            // 1. 尝试直接查找 JSON.parse('...')
            Pattern jsonParsePattern = Pattern.compile("JSON\\.parse\\('([^']+)'\\)");
            var matcher = jsonParsePattern.matcher(rawScriptContent);

            while (matcher.find()) {
                String encoded = matcher.group(1);
                try {
                    String decoded = decodeJsonString(encoded);
                    JsonNode node = mapper.readTree(decoded);
                    if (isValidData(node)) {
                        return node;
                    }
                } catch (Exception e) {
                    // 继续尝试下一个匹配
                }
            }

            // 2. 尝试查找 var data = {...} 格式
            Pattern varPattern = Pattern.compile("var\\s+\\w+\\s*=\\s*(\\{.*?\\})\\s*;", Pattern.DOTALL);
            matcher = varPattern.matcher(rawScriptContent);

            if (matcher.find()) {
                String jsonStr = matcher.group(1);
                return mapper.readTree(jsonStr);
            }

            // 3. 尝试查找数据数组
            Pattern arrayPattern = Pattern.compile("\\[\\s*\\{.*?\\}\\s*\\]", Pattern.DOTALL);
            matcher = arrayPattern.matcher(rawScriptContent);

            while (matcher.find()) {
                try {
                    String arrayStr = matcher.group(0);
                    return mapper.readTree(arrayStr);
                } catch (Exception e) {
                    // 继续尝试
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapper.createObjectNode();
    }

    /**
     * 解码JSON字符串
     */
    private static String decodeJsonString(String encoded) {
        // 处理常见的转义序列
        return encoded
                .replace("\\\"", "\"")
                .replace("\\'", "'")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\b", "\b")
                .replace("\\f", "\f");
    }

    /**
     * 验证数据是否有效
     */
    private static boolean isValidData(JsonNode node) {
        if (node == null) return false;
        if (node.isArray() && node.size() > 0) return true;
        if (node.isObject() && !node.isEmpty()) return true;
        return false;
    }

    /**
     * 转换球员数据为结构化对象
     */
    public static List<PlayerStats> parsePlayerStats(JsonNode data) {
        List<PlayerStats> players = new ArrayList<>();

        if (data != null && data.isArray()) {
            for (JsonNode playerNode : data) {
                PlayerStats stats = new PlayerStats();

                stats.setPlayerName(getText(playerNode, "player_name"));
                stats.setTeam(getText(playerNode, "team_title"));
                stats.setGames(getInt(playerNode, "games"));
                stats.setGoals(getInt(playerNode, "goals"));
                stats.setAssists(getInt(playerNode, "assists"));
                stats.setXg(getDouble(playerNode, "xG"));
                stats.setXa(getDouble(playerNode, "xA"));
                stats.setNpxg(getDouble(playerNode, "npxG"));
                stats.setXg90(getDouble(playerNode, "xG90"));
                stats.setXa90(getDouble(playerNode, "xA90"));
                stats.setXgChain(getDouble(playerNode, "xGChain"));
                stats.setXgBuildup(getDouble(playerNode, "xGBuildup"));

                players.add(stats);
            }
        }

        return players;
    }

    private static String getText(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asText() : "";
    }

    private static int getInt(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asInt() : 0;
    }

    private static double getDouble(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asDouble() : 0.0;
    }
}