package cn.xingxing.data.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class EPLDataGenerator {

    // 定义球队数据类
    static class TeamStats {
        int number;
        String team;
        int matches;
        int wins;
        int draws;
        int loses;
        int goals;
        int ga;
        int points;
        double xG;
        double NPxG;
        double xGA;
        double NPxGA;
        double NPxGD;
        double ppda;
        double ppda_allowed;
        int deep;
        int deep_allowed;
        double xPTS;

        // 用于主客场统计
        boolean isHome;

        public TeamStats(String team) {
            this.team = team;
            this.number = 0;
            this.matches = 0;
            this.wins = 0;
            this.draws = 0;
            this.loses = 0;
            this.goals = 0;
            this.ga = 0;
            this.points = 0;
            this.xG = 0.0;
            this.NPxG = 0.0;
            this.xGA = 0.0;
            this.NPxGA = 0.0;
            this.NPxGD = 0.0;
            this.ppda = 0.0;
            this.ppda_allowed = 0.0;
            this.deep = 0;
            this.deep_allowed = 0;
            this.xPTS = 0.0;
            this.isHome = false;
        }

        // 添加一场比赛数据
        public void addMatch(boolean isHome, Map<String, Object> matchData) {
            this.matches++;
            this.isHome = isHome;

            // 比赛结果
            String result = (String) matchData.get("result");
            if ("w".equals(result)) {
                this.wins++;
                this.points += 3;
            } else if ("d".equals(result)) {
                this.draws++;
                this.points += 1;
            } else if ("l".equals(result)) {
                this.loses++;
            }

            // 进球和失球
            this.goals += (int) matchData.get("scored");
            this.ga += (int) matchData.get("missed");

            // 预期数据
            this.xG += ((Number) matchData.get("xG")).doubleValue();
            this.NPxG += ((Number) matchData.get("npxG")).doubleValue();
            this.xGA += ((Number) matchData.get("xGA")).doubleValue();
            this.NPxGA += ((Number) matchData.get("npxGA")).doubleValue();
            this.xPTS += ((Number) matchData.get("xpts")).doubleValue();

            // 深度传球
            this.deep += (int) matchData.get("deep");
            this.deep_allowed += (int) matchData.get("deep_allowed");

            // PPDA数据
            Map<String, Object> ppdaData = (Map<String, Object>) matchData.get("ppda");
            Map<String, Object> ppdaAllowedData = (Map<String, Object>) matchData.get("ppda_allowed");

            int ppdaAtt = (int) ppdaData.get("att");
            int ppdaDef = (int) ppdaData.get("def");
            int ppdaAllowedAtt = (int) ppdaAllowedData.get("att");
            int ppdaAllowedDef = (int) ppdaAllowedData.get("def");

            // 计算PPDA值（如果def为0，避免除零错误）
            double ppdaValue = ppdaDef > 0 ? (double) ppdaAtt / ppdaDef : 0;
            double ppdaAllowedValue = ppdaAllowedDef > 0 ? (double) ppdaAllowedAtt / ppdaAllowedDef : 0;

            // 累加PPDA值，后续计算平均值
            this.ppda += ppdaValue;
            this.ppda_allowed += ppdaAllowedValue;
        }

        // 完成统计后计算平均值
        public void finalizeStats() {
            if (matches > 0) {
                this.ppda /= matches;
                this.ppda_allowed /= matches;
                this.NPxGD = this.NPxG - this.NPxGA;
            }
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>(); // 使用LinkedHashMap保持顺序
            map.put("number", number);
            map.put("team", team);
            map.put("matches", matches);
            map.put("wins", wins);
            map.put("draws", draws);
            map.put("loses", loses);
            map.put("goals", goals);
            map.put("ga", ga);
            map.put("points", points);
            map.put("xG", Math.round(xG * 100.0) / 100.0);
            map.put("NPxG", Math.round(NPxG * 100.0) / 100.0);
            map.put("xGA", Math.round(xGA * 100.0) / 100.0);
            map.put("NPxGA", Math.round(NPxGA * 100.0) / 100.0);
            map.put("NPxGD", Math.round(NPxGD * 100.0) / 100.0);
            map.put("ppda", Math.round(ppda * 100.0) / 100.0);
            map.put("ppda_allowed", Math.round(ppda_allowed * 100.0) / 100.0);
            map.put("deep", deep);
            map.put("deep_allowed", deep_allowed);
            map.put("xPTS", Math.round(xPTS * 100.0) / 100.0);
            return map;
        }
    }

    public static String readResourceFile(String fileName) throws IOException {
        // 尝试从保存数据的目录读取（与saveToFile使用相同的逻辑）
        String content = readFromDataDir(fileName);
        if (content != null) {
            return content;
        }

        // 如果数据目录不存在或文件不存在，则回退到classpath资源目录
        return readFromClasspath(fileName);
    }

    public static String readFromDataDir(String fileName) throws IOException {
        try {
            URL resourceUrl = EPLDataGenerator.class.getClassLoader().getResource("");
            File dataDir =new File("/app/data");

            /*if (resourceUrl != null && resourceUrl.getProtocol().equals("file")) {
                // 开发环境：资源在文件系统中
                String decodedPath = URLDecoder.decode(resourceUrl.getPath(), StandardCharsets.UTF_8);
                File resourcesDir = new File(decodedPath);
                dataDir = new File(resourcesDir, "data");
            } else {
                // 生产环境（jar包）或无法获取资源目录时，使用当前工作目录下的data目录
                dataDir = new File("data");
            }*/

            File inputFile = new File(dataDir, fileName);

            if (inputFile.exists()) {
                return readFileContent(inputFile);
            } else {
                return null; // 文件不存在于数据目录
            }
        } catch (Exception e) {
            // 读取数据目录失败时，回退到classpath
            return null;
        }
    }

    private static String readFromClasspath(String fileName) throws IOException {
        // 先尝试直接从根目录读取
        InputStream inputStream = EPLDataGenerator.class.getClassLoader().getResourceAsStream(fileName);

        // 如果找不到，再尝试从data子目录读取
        if (inputStream == null) {
            inputStream = EPLDataGenerator.class.getClassLoader().getResourceAsStream("data/" + fileName);
        }

        if (inputStream == null) {
            throw new FileNotFoundException("Resource file not found: " + fileName);
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    // 写入文件到指定目录（修复Windows路径问题）
    private static void writeToResourceDir(String fileName, String content) throws IOException {
        try {
            // 获取当前类所在的目录
            URL classUrl = EPLDataGenerator.class.getProtectionDomain().getCodeSource().getLocation();
            Path classDir;

            // 处理JAR文件的情况
            if (classUrl.toString().endsWith(".jar")) {
                // 如果是JAR文件，写入到JAR文件所在目录
                Path jarPath = Paths.get(classUrl.toURI()).getParent();
                classDir = jarPath;
            } else {
                // 如果是IDE中运行，写入到target/classes目录
                classDir = Paths.get("target", "classes");
                // 如果target/classes不存在，尝试创建
                if (!Files.exists(classDir)) {
                    Files.createDirectories(classDir);
                }
            }

            // 构建输出路径
            Path outputPath = classDir.resolve(fileName);
            Files.createDirectories(outputPath.getParent());

            // 写入文件
            Files.write(outputPath, content.getBytes("UTF-8"));
            System.out.println("文件已写入: " + outputPath.toAbsolutePath());

        } catch (URISyntaxException e) {
            // 如果上面的方法失败，回退到当前工作目录
            Path fallbackDir = Paths.get("").toAbsolutePath();
            Path outputPath = fallbackDir.resolve("output").resolve(fileName);
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, content.getBytes("UTF-8"));
            System.out.println("文件已写入（备用路径）: " + outputPath.toAbsolutePath());
        }
    }

    // 更简单的方法：直接写入到当前目录的output文件夹
    private static void writeToOutputDir(String fileName, String content) throws IOException {
        Path outputDir = Paths.get("output");
        Files.createDirectories(outputDir);

        Path outputPath = outputDir.resolve(fileName);
        Files.write(outputPath, content.getBytes("UTF-8"));
        System.out.println("文件已写入到output目录: " + outputPath.toAbsolutePath());
    }


    private static void saveToFile( String filename,String data) {
        try {
            // 获取classpath的根目录路径（资源目录）
           // URL resourceUrl = UnderstatScraper.class.getClassLoader().getResource("");
            File dataDir =new File("/app/data");

         /*   if (resourceUrl != null && resourceUrl.getProtocol().equals("file")) {
                // 开发环境：资源在文件系统中
                String decodedPath = URLDecoder.decode(resourceUrl.getPath(), StandardCharsets.UTF_8);
                File resourcesDir = new File(decodedPath);
                dataDir = new File(resourcesDir, "data");
            } else {
                // 生产环境（jar包）或无法获取资源目录时，使用当前工作目录下的data目录
                dataDir = new File("data");
            }
*/
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            File outputFile = new File(dataDir, filename);

            try (FileWriter file = new FileWriter(outputFile)) {
                file.write(data);
                System.out.println("数据已保存到: " + outputFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("保存文件失败: " + e.getMessage());
        }
    }

    public static void saveXgData(String league) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            // 1. 从resource目录读取原始数据
            String jsonContent = readResourceFile(league+"_2025_teams.json");
            Map<String, Map<String, Object>> teamsData = mapper.readValue(
                    jsonContent,
                    new TypeReference<Map<String, Map<String, Object>>>() {}
            );

            // 2. 处理数据
            Map<String, TeamStats> totalStats = new HashMap<>();
            Map<String, TeamStats> homeStats = new HashMap<>();
            Map<String, TeamStats> awayStats = new HashMap<>();

            for (Map.Entry<String, Map<String, Object>> teamEntry : teamsData.entrySet()) {
                String teamId = teamEntry.getKey();
                Map<String, Object> teamData = teamEntry.getValue();
                String teamName = (String) teamData.get("title");
                List<Map<String, Object>> history = (List<Map<String, Object>>) teamData.get("history");

                // 初始化统计对象
                totalStats.put(teamName, new TeamStats(teamName));
                homeStats.put(teamName, new TeamStats(teamName));
                awayStats.put(teamName, new TeamStats(teamName));

                // 处理每场比赛
                for (Map<String, Object> match : history) {
                    String venue = (String) match.get("h_a");
                    boolean isHome = "h".equals(venue);

                    // 更新总统计
                    totalStats.get(teamName).addMatch(isHome, match);

                    // 更新主客场统计
                    if (isHome) {
                        homeStats.get(teamName).addMatch(true, match);
                    } else {
                        awayStats.get(teamName).addMatch(false, match);
                    }
                }
            }

            // 3. 完成统计计算
            for (TeamStats stats : totalStats.values()) {
                stats.finalizeStats();
            }
            for (TeamStats stats : homeStats.values()) {
                stats.finalizeStats();
            }
            for (TeamStats stats : awayStats.values()) {
                stats.finalizeStats();
            }

            // 4. 转换为列表并排序（按积分）
            List<Map<String, Object>> totalList = new ArrayList<>();
            List<Map<String, Object>> homeList = new ArrayList<>();
            List<Map<String, Object>> awayList = new ArrayList<>();

            // 总数据排序
            List<TeamStats> sortedTotal = new ArrayList<>(totalStats.values());
            sortedTotal.sort((a, b) -> {
                if (b.points != a.points) return b.points - a.points;
                // 积分相同按净胜球排序
                int goalDiffA = a.goals - a.ga;
                int goalDiffB = b.goals - b.ga;
                if (goalDiffB != goalDiffA) return goalDiffB - goalDiffA;
                // 净胜球相同按进球数排序
                return b.goals - a.goals;
            });

            int rank = 1;
            for (TeamStats stats : sortedTotal) {
                stats.number = rank++;
                totalList.add(stats.toMap());
            }

            // 主场数据排序
            List<TeamStats> sortedHome = new ArrayList<>(homeStats.values());
            sortedHome.sort((a, b) -> {
                if (b.points != a.points) return b.points - a.points;
                // 积分相同按净胜球排序
                int goalDiffA = a.goals - a.ga;
                int goalDiffB = b.goals - b.ga;
                if (goalDiffB != goalDiffA) return goalDiffB - goalDiffA;
                return b.goals - a.goals;
            });

            rank = 1;
            for (TeamStats stats : sortedHome) {
                stats.number = rank++;
                homeList.add(stats.toMap());
            }

            // 客场数据排序
            List<TeamStats> sortedAway = new ArrayList<>(awayStats.values());
            sortedAway.sort((a, b) -> {
                if (b.points != a.points) return b.points - a.points;
                // 积分相同按净胜球排序
                int goalDiffA = a.goals - a.ga;
                int goalDiffB = b.goals - b.ga;
                if (goalDiffB != goalDiffA) return goalDiffB - goalDiffA;
                return b.goals - a.goals;
            });

            rank = 1;
            for (TeamStats stats : sortedAway) {
                stats.number = rank++;
                awayList.add(stats.toMap());
            }

            // 5. 写入文件（使用简单的方法，写入到output目录）
            String totalJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(totalList);
            String homeJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(homeList);
            String awayJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(awayList);

            // 使用简单的方法写入到output目录
            saveToFile(league+".json", totalJson);
            saveToFile(league+"_home.json", homeJson);
            saveToFile(league+"_away.json", awayJson);

            System.out.println("\n数据生成完成！");
            System.out.println("已生成文件：");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("处理JSON文件时出错: " + e.getMessage());
        }
    }
}