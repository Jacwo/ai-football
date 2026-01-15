package cn.xingxing.data.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class UnderstatScraper {

    private static final String BASE_URL = "https://understat.com";
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static Map<String, JsonNode> scrapeWithSelenium(String league, String season) {
        Map<String, JsonNode> result = new HashMap<>();
        WebDriver driver = null;

        try {
           /*String path = UnderstatScraper.class.getClassLoader().getResource("tools/chromedriver.exe").getPath();
            if (path.startsWith("/") && path.charAt(2) == ':') {
                path = path.substring(1);
            }
            log.info("path:" + path);*/
            // 设置ChromeDriver路径（需要先下载chromedriver）
            // "D:\\personal\\ai-football\\ai-football-starter\\src\\main\\resources\\tools\\chromedriver.exe"

            System.setProperty("webdriver.chrome.driver","/app/tools/chromedriver.exe");
          //  WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
           // ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless"); // 无头模式
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            driver = new ChromeDriver(options);
            String url = String.format("%s/league/%s/%s", BASE_URL, league, season);
            driver.get(url);

            // 等待页面加载
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#league-players")));

            // 等待数据加载完成
            Thread.sleep(5000);

            // 方法2：执行JavaScript获取全局变量
            result.putAll(extractDataViaJavaScript(driver));

        } catch (Exception e) {
            log.error(e.getMessage());
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
               // "playersData",
                "teamsData"/*, "tableChempData",
                "home", "away", "tableData"*/
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


}