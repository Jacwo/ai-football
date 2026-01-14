package cn.xingxing.common.util.data;



import com.fasterxml.jackson.databind.JsonNode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-25
 * @Version: 1.0
 */
public class UnderstatAPI {
    public static void main(String[] args) {
        try {
            // 示例1：获取英超2025赛季数据
            System.out.println("正在获取英超2025赛季数据...");

          //  String data1 = UnderstatScraper.getRawPageContent("EPL", 2025);
          //  System.out.println("<UNK>" + data1);
            //EPL 英超 La_liga 西甲
            Map<String, JsonNode> data = UnderstatScraper.scrapeWithSelenium("La_liga", 2025);

            if (!data.isEmpty()) {
                System.out.println("成功获取到 " + data.size() + " 个数据集");

                // 处理球员数据
                if (data.containsKey("playersData")) {
                    JsonNode playersData = data.get("playersData");
                    List<PlayerStats> players = UnderstatDataParser.parsePlayerStats(playersData);

                    System.out.println("\n英超2025赛季球员数据（前10名）：");
                    System.out.println("==========================================");
                    for (int i = 0; i < Math.min(10, players.size()); i++) {
                        PlayerStats player = players.get(i);
                        System.out.printf("%2d. %-25s %-20s xG: %.2f, xA: %.2f, 进球: %d, 助攻: %d%n",
                                i + 1, player.getPlayerName(), player.getTeam(),
                                player.getXg(), player.getXa(),
                                player.getGoals(), player.getAssists());
                    }

                    // 保存到文件
                    saveToFile(playersData, "epl_2025_players.json");
                }

                // 处理球队数据
                if (data.containsKey("teamsData")) {
                    JsonNode teamsData = data.get("teamsData");
                    // 解析球队数据...
                    saveToFile(teamsData, "epl_2025_teams.json");
                }
            }

            // 示例2：获取球员数据
            System.out.println("\n正在获取球员数据...");
            JsonNode playerData = UnderstatScraper.getPlayerStats(12345); // 替换为真实球员ID
            if (playerData != null) {
                System.out.println("球员数据获取成功");
                saveToFile(playerData, "player_12345.json");
            }

            // 示例3：使用Selenium获取数据（如果需要JavaScript渲染）
            System.out.println("\n使用Selenium获取完整数据...");
            Map<String, JsonNode> fullData = UnderstatScraper.scrapeWithSelenium("La_liga", 2025);
            if (!fullData.isEmpty()) {
                System.out.println("Selenium获取到 " + fullData.size() + " 个数据集");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveToFile(JsonNode data, String filename) {
        try (FileWriter file = new FileWriter(filename)) {
            file.write(data.toPrettyString());
            System.out.println("数据已保存到: " + filename);
        } catch (IOException e) {
            System.err.println("保存文件失败: " + e.getMessage());
        }
    }
}
