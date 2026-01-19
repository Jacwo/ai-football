package cn.xingxing.data;


import cn.xingxing.data.util.EPLDataGenerator;
import cn.xingxing.data.util.UnderstatScraper;
import cn.xingxing.entity.TeamStats;
import cn.xingxing.mapper.TeamStatsMapper;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: yangyuanliang
 * @Date: 2025-12-25
 * @Version: 1.0
 */
@Service
public class TeamStatsServiceImpl implements TeamStatsService {
    @Autowired
    private TeamStatsMapper teamStatsMapper;

    @Override
    public void loadTeamStats(String name) {
        try {
            String fileName =  name + ".json";
            String jsonContent = EPLDataGenerator.readFromDataDir(fileName);

          //  String jsonContent = new String(inputStream.readAllBytes());
            List<TeamStats> teamStatsList = JSONObject.parseArray(jsonContent, TeamStats.class);

            System.out.println("成功读取 " + teamStatsList.size() + " 支球队数据");
            List<TeamStats> newTeamStatsList = new ArrayList<>();
            AtomicInteger rank = new AtomicInteger(1);
            teamStatsList.forEach(teamStats -> {
                TeamStats teamStatsDB = selectByTeam(teamStats.getTeam(), "all");
                teamStats.setRankNum(rank.getAndIncrement());
                if (teamStatsDB != null) {
                    teamStats.setTeamName(teamStatsDB.getTeamName());
                    teamStats.setId(teamStatsDB.getId());
                    BeanUtils.copyProperties(teamStats, teamStatsDB);
                    newTeamStatsList.add(teamStatsDB);
                } else {
                    teamStats.setFlag("all");
                    newTeamStatsList.add(teamStats);
                }
            });
            teamStatsMapper.insertOrUpdate(newTeamStatsList);
        } catch (Exception e) {
            System.err.println("导入数据时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据导入失败", e);
        }

    }

    @Override
    public TeamStats selectByTeamName(String teamName, String flag) {
        LambdaQueryWrapper<TeamStats> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeamStats::getTeamName, teamName);
        queryWrapper.eq(TeamStats::getFlag, flag);

        return teamStatsMapper.selectOne(queryWrapper);
    }

    @Override
    public TeamStats selectByTeam(String team, String flag) {
        LambdaQueryWrapper<TeamStats> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeamStats::getTeam, team);
        queryWrapper.eq(TeamStats::getFlag, flag);
        return teamStatsMapper.selectOne(queryWrapper);
    }

    @Override
    public void loadTeamStatsHome(String name) {
        try {
            String fileName =  name + "_home.json";

            String jsonContent = EPLDataGenerator.readFromDataDir(fileName);


           // String jsonContent = new String(inputStream.readAllBytes());
            List<TeamStats> teamStatsList = JSONObject.parseArray(jsonContent, TeamStats.class);

            System.out.println("成功读取 " + teamStatsList.size() + " 支球队数据");
            List<TeamStats> newTeamStatsList = new ArrayList<>();
            teamStatsList.forEach(teamStats -> {
                TeamStats teamStatsDB = selectByTeam(teamStats.getTeam(), "home");
                if (teamStatsDB != null) {
                    teamStats.setTeamName(teamStatsDB.getTeamName());
                    teamStats.setId(teamStatsDB.getId());
                    BeanUtils.copyProperties(teamStats, teamStatsDB);
                    newTeamStatsList.add(teamStatsDB);
                } else {
                    teamStats.setFlag("home");
                    newTeamStatsList.add(teamStats);
                }
            });
            teamStatsMapper.insertOrUpdate(newTeamStatsList);
        } catch (Exception e) {
            System.err.println("导入数据时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据导入失败", e);
        }
    }

    @Override
    public void loadTeamStatsAway(String name) {
        try {
            String fileName = name + "_away.json";
            String jsonContent = EPLDataGenerator.readFromDataDir(fileName);
            List<TeamStats> teamStatsList = JSONObject.parseArray(jsonContent, TeamStats.class);

            System.out.println("成功读取 " + teamStatsList.size() + " 支球队数据");
            List<TeamStats> newTeamStatsList = new ArrayList<>();
            teamStatsList.forEach(teamStats -> {
                TeamStats teamStatsDB = selectByTeam(teamStats.getTeam(), "away");
                if (teamStatsDB != null) {
                    teamStats.setTeamName(teamStatsDB.getTeamName());
                    teamStats.setId(teamStatsDB.getId());
                    BeanUtils.copyProperties(teamStats, teamStatsDB);
                    newTeamStatsList.add(teamStatsDB);
                } else {
                    teamStats.setFlag("away");
                    newTeamStatsList.add(teamStats);
                }
            });
            teamStatsMapper.insertOrUpdate(newTeamStatsList);
        } catch (Exception e) {
            System.err.println("导入数据时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据导入失败", e);
        }
    }

    @Override
    public void syncXgData(String league, String year) {
        Map<String, JsonNode> data = UnderstatScraper.scrapeWithSelenium(league, year);
        if (!data.isEmpty()) {
            System.out.println("成功获取到 " + data.size() + " 个数据集");

            // 处理球员数据
        /*        if (data.containsKey("playersData")) {
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
*/
            // 处理球队数据
            if (data.containsKey("teamsData")) {
                JsonNode teamsData = data.get("teamsData");
                // 解析球队数据...
                saveToFile(teamsData, league+"_2025_teams.json");
            }
        }


    }

    private static void saveToFile(JsonNode data, String filename) {
        try {
            // 获取classpath的根目录路径（资源目录）
        //    URL resourceUrl = UnderstatScraper.class.getClassLoader().getResource("");
            File dataDir = new File("/app/data");

           /* if (resourceUrl != null && resourceUrl.getProtocol().equals("file")) {
                // 开发环境：资源在文件系统中
                String decodedPath = URLDecoder.decode(resourceUrl.getPath(), StandardCharsets.UTF_8);
                File resourcesDir = new File(decodedPath);
                dataDir = new File(resourcesDir, "data");
            } else {
                // 生产环境（jar包）或无法获取资源目录时，使用当前工作目录下的data目录
                dataDir = new File("data");
            }*/

            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            File outputFile = new File(dataDir, filename);

            try (FileWriter file = new FileWriter(outputFile)) {
                file.write(data.toPrettyString());
                System.out.println("数据已保存到: " + outputFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("保存文件失败: " + e.getMessage());
        }
    }
}
