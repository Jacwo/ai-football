package cn.xingxing.common.util.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnderstatScraper {

    private static final String BASE_URL = "https://understat.com";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern JSON_DATA_PATTERN =
            Pattern.compile("JSON\\.parse\\('([^']+)'\\)");
    private static final Pattern VAR_DATA_PATTERN =
            Pattern.compile("(playersData|teamsData|rostersData|matchesData)\\s*=\\s*JSON\\.parse\\('([^']+)'\\)");

    /**
     * 方式1：使用Jsoup直接解析页面（推荐，不需要浏览器）
     * 这个方法通过分析页面中的script标签来提取数据
     */
    public static Map<String, JsonNode> scrapeLeaguePage(String league, int season) throws IOException {
        String url = String.format("%s/league/%s/%d", BASE_URL, league, season);

        // 设置请求头，模拟浏览器
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Language", "en-US,en;q=0.5");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Connection", "keep-alive");
        headers.put("Upgrade-Insecure-Requests", "1");

        Document doc = Jsoup.connect(url)
                .headers(headers)
                .timeout(30000)
                .get();

        return extractAllDataFromPage(doc);
    }

    /**
     * 从页面中提取所有数据
     */
    private static Map<String, JsonNode> extractAllDataFromPage(Document doc) {
        Map<String, JsonNode> result = new HashMap<>();

        // 查找所有的script标签
        Elements scripts = doc.select("script");
        for (Element script : scripts) {
            String scriptContent = script.html();
            if (scriptContent.trim().isEmpty()) {
                continue;
            }

            // 方法1：尝试匹配 JSON.parse('...') 格式
            Matcher jsonMatcher = JSON_DATA_PATTERN.matcher(scriptContent);
            while (jsonMatcher.find()) {
                try {
                    String encodedJson = jsonMatcher.group(1);
                    JsonNode jsonNode = parseEncodedJson(encodedJson);
                    if (jsonNode != null && jsonNode.size() > 0) {
                        result.put("data_" + result.size(), jsonNode);
                    }
                } catch (Exception e) {
                    // 忽略解析错误，继续尝试
                }
            }

            // 方法2：尝试匹配 playersData = JSON.parse('...') 格式
            Matcher varMatcher = VAR_DATA_PATTERN.matcher(scriptContent);
            while (varMatcher.find()) {
                try {
                    String varName = varMatcher.group(1);
                    String encodedJson = varMatcher.group(2);
                    JsonNode jsonNode = parseEncodedJson(encodedJson);
                    if (jsonNode != null) {
                        result.put(varName, jsonNode);
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }

            // 方法3：尝试查找其他常见的数据变量
            extractCommonDataVariables(scriptContent, result);
        }

        return result;
    }

    /**
     * 解析编码的JSON字符串
     */
    private static JsonNode parseEncodedJson(String encodedJson) throws IOException {
        if (encodedJson == null || encodedJson.isEmpty()) {
            return null;
        }

        // 解码JSON字符串中的转义字符
        String decodedJson = encodedJson
                .replace("\\u0022", "\"")
                .replace("\\u0027", "'")
                .replace("\\u005C", "\\")
                .replace("\\/", "/")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");

        // 有时数据是双重编码的，需要尝试多层解析
        try {
            return objectMapper.readTree(decodedJson);
        } catch (Exception e1) {
            // 如果第一次解析失败，尝试清理数据
            try {
                // 移除可能的JavaScript注释
                String cleaned = decodedJson.replaceAll("//.*?\n", "")
                        .replaceAll("/\\*.*?\\*/", "");
                return objectMapper.readTree(cleaned);
            } catch (Exception e2) {
                System.err.println("Failed to parse JSON: " + e2.getMessage());
                return null;
            }
        }
    }

    /**
     * 提取常见的数据变量
     */
    private static void extractCommonDataVariables(String scriptContent, Map<String, JsonNode> result) {
        // 匹配各种可能的数据变量格式
        String[] varPatterns = {
                "data\\s*=\\s*JSON\\.parse\\('([^']+)'\\)",
                "statsData\\s*=\\s*JSON\\.parse\\('([^']+)'\\)",
                "tableData\\s*=\\s*JSON\\.parse\\('([^']+)'\\)",
                "rosterData\\s*=\\s*JSON\\.parse\\('([^']+)'\\)"
        };

        for (String pattern : varPatterns) {
            Matcher matcher = Pattern.compile(pattern).matcher(scriptContent);
            if (matcher.find()) {
                try {
                    String encodedJson = matcher.group(1);
                    JsonNode jsonNode = parseEncodedJson(encodedJson);
                    if (jsonNode != null) {
                        String varName = pattern.split("\\\\s*=\\\\s*")[0];
                        result.put(varName, jsonNode);
                    }
                } catch (Exception e) {
                    // 忽略错误
                }
            }
        }
    }

    /**
     * 方式2：使用HTTP客户端获取原始响应（更接近浏览器请求）
     */
    public static String getRawPageContent(String league, int season) throws IOException {
        String url = String.format("%s/league/%s/%d", BASE_URL, league, season);

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()) {

            HttpGet request = new HttpGet(url);
            request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            request.addHeader("Accept-Language", "en-US,en;q=0.5");
            request.addHeader("Accept-Encoding", "gzip, deflate, br");
            request.addHeader("Connection", "keep-alive");

            return EntityUtils.toString(httpClient.execute(request).getEntity());
        }
    }

    /**
     * 方式3：使用Selenium获取完整渲染的页面（最可靠但最慢）
     */
    public static Map<String, JsonNode> scrapeWithSelenium(String league, int season) {
        Map<String, JsonNode> result = new HashMap<>();
        WebDriver driver = null;

        try {
            // 设置ChromeDriver路径（需要先下载chromedriver）
            System.setProperty("webdriver.chrome.driver", "D:\\personal\\ai-football\\tools\\chromedriver.exe");

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless"); // 无头模式
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            driver = new ChromeDriver(options);
            String url = String.format("%s/league/%s/%d", BASE_URL, league, season);
            driver.get(url);

            // 等待页面加载
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#league-players")));

            // 等待数据加载完成
            Thread.sleep(5000);

            // 方法1：通过JavaScript直接获取数据
            String pageSource = driver.getPageSource();
            Document doc = Jsoup.parse(pageSource);
            result.putAll(extractAllDataFromPage(doc));

            // 方法2：执行JavaScript获取全局变量
            result.putAll(extractDataViaJavaScript(driver));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

        return result;
    }

    /**
     * 通过JavaScript执行获取数据
     */
    private static Map<String, JsonNode> extractDataViaJavaScript(WebDriver driver) {
        Map<String, JsonNode> result = new HashMap<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // 尝试获取各种可能的数据变量
        String[] dataVars = {
                "playersData", "teamsData", "matchesData",
                "rostersData", "statsData", "tableData"
        };

        for (String varName : dataVars) {
            try {
                Object data = js.executeScript(
                        "if (typeof " + varName + " !== 'undefined') {" +
                                "  return JSON.stringify(" + varName + ");" +
                                "} else {" +
                                "  return null;" +
                                "}"
                );

                if (data != null) {
                    JsonNode jsonNode = objectMapper.readTree(data.toString());
                    result.put(varName, jsonNode);
                }
            } catch (Exception e) {
                // 变量不存在，继续尝试下一个
            }
        }

        return result;
    }

    /**
     * 提取球员数据
     */
    public static JsonNode getPlayerStats(int playerId) throws IOException {
        String url = String.format("%s/player/%d", BASE_URL, playerId);

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get();

        Map<String, JsonNode> data = extractAllDataFromPage(doc);
        return data.getOrDefault("playersData",
                data.getOrDefault("matchesData",
                        data.getOrDefault("statsData", null)));
    }

    /**
     * 提取球队数据
     */
    public static JsonNode getTeamStats(String teamName, int season) throws IOException {
        // 清理球队名称中的特殊字符
        String sanitizedTeamName = teamName.replace(" ", "_").replace("&", "and");
        String url = String.format("%s/team/%s/%d", BASE_URL, sanitizedTeamName, season);

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get();

        Map<String, JsonNode> data = extractAllDataFromPage(doc);
        return data.getOrDefault("teamsData",
                data.getOrDefault("matchesData",
                        data.getOrDefault("statsData", null)));
    }
}