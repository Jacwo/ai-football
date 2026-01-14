package cn.xingxing.common.util.data;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.interactions.Actions;
import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class UnderstatSeleniumExporter {

    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;
    private String downloadDir;

    public UnderstatSeleniumExporter(String downloadDir) {
        this.downloadDir = downloadDir;
        setupDriver();
    }

    private void setupDriver() {
        // 设置Chrome驱动路径
        System.setProperty("webdriver.chrome.driver", "D:\\personal\\ai-football\\tools\\chromedriver.exe");

        // 配置Chrome选项
        ChromeOptions options = new ChromeOptions();

        // 设置下载目录
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadDir);
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("safebrowsing.enabled", true);
        options.setExperimentalOption("prefs", prefs);

        // 可选：无头模式
        // options.addArguments("--headless");

        // 其他选项
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-notifications");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        actions = new Actions(driver);

        // 设置隐式等待
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    /**
     * 导出指定联赛和赛季的数据
     * @param league 联赛代码 (EPL, La_Liga, Serie_A, Bundesliga, Ligue_1, RFPL)
     * @param year 赛季年份
     */
    public void exportLeagueData(String league, int year) {
        try {
            // 1. 访问页面
            String url = String.format("https://understat.com/league/%s/%d", league, year);
            System.out.println("访问页面: " + url);
            driver.get(url);

            // 等待页面加载完成
            waitForPageLoad();

            // 2. 点击导出按钮打开下拉菜单
            clickExportButton();

            // 3. 依次导出三种类型的数据
           // exportByType("Overall", league, year);
       //     exportByType("Home", league, year);
        //    exportByType("Away", league, year);

            System.out.println("✅ 数据导出完成！");

        } catch (Exception e) {
            System.err.println("导出过程中出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 等待页面加载完成
     */
    private void waitForPageLoad() {
        // 等待页面标题出现
        wait.until(ExpectedConditions.titleContains("Understat"));

        // 等待表格加载完成
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("table tbody tr")
        ));

        // 等待JavaScript加载完成
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'"));

        // 短暂等待确保所有元素加载完成
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 点击导出按钮
     */
    private void clickExportButton() {
        try {
            // 方案1: 直接点击导出按钮（如果按钮存在）
            try {
                WebElement exportBtn = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(), 'export')]")
                ));
                exportBtn.click();
                System.out.println("找到并点击了导出按钮");
                Thread.sleep(1000);
                return;
            } catch (Exception e) {
                // 如果找不到，继续尝试其他方案
            }

            // 方案2: 查找导出相关的下拉菜单
            /*List<WebElement> buttons = driver.findElements(By.tagName("button"));
            for (WebElement btn : buttons) {
                if (btn.getText().toLowerCase().contains("export") ||
                        btn.getAttribute("class").toLowerCase().contains("export")) {
                    actions.moveToElement(btn).click().perform();
                    System.out.println("通过class/文本找到了导出按钮");
                    Thread.sleep(1000);
                    return;
                }
            }*/

            // 方案3: 使用JavaScript点击可能的导出元素
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String script = "var buttons = document.getElementsByTagName('button');" +
                    "for(var i=0; i<buttons.length; i++) {" +
                    "  var btn = buttons[i];" +
                    "  var text = btn.innerText || btn.textContent;" +
                    "  if(text && text.toLowerCase().includes('export')) {" +
                    "    btn.click(); return true;" +
                    "  }" +
                    "}" +
                    "// 尝试查找dropdown或menu" +
                    "var menus = document.querySelectorAll('[role=\"menu\"], .dropdown-menu');" +
                    "if(menus.length > 0) {" +
                    "  var firstMenu = menus[0];" +
                    "  firstMenu.style.display = 'block'; return true;" +
                    "}" +
                    "return false;";

            Boolean result = (Boolean) js.executeScript(script);
            if (result) {
                System.out.println("通过JavaScript找到了导出按钮");
                Thread.sleep(1000);
                return;
            }

            // 方案4: 如果实在找不到按钮，直接模拟下载链接
            System.out.println("⚠️ 未找到导出按钮，将尝试直接下载文件...");

        } catch (Exception e) {
            System.err.println("点击导出按钮时出错: " + e.getMessage());
        }
    }

    /**
     * 按类型导出数据
     */
    private void exportByType(String type, String league, int year) {
        try {
            System.out.println("开始导出 " + type + " 数据...");

            // 1. 尝试通过点击菜单项导出
            if (tryClickExportMenuItem(type)) {
                waitForFileDownload(type, league, year);
                return;
            }

            // 2. 如果点击菜单失败，尝试直接下载
            System.out.println("尝试直接下载 " + type + " 数据...");
            directDownload(league, year, type);

        } catch (Exception e) {
            System.err.println("导出 " + type + " 数据时出错: " + e.getMessage());
        }
    }

    /**
     * 尝试点击导出菜单项
     */
    private boolean tryClickExportMenuItem(String type) {
        try {
            // 查找包含类型的菜单项
            String[] searchTexts = {
                    type.toLowerCase(),
                    type.substring(0, 1).toUpperCase() + type.substring(1)
            };

            for (String searchText : searchTexts) {
                try {
                    // 查找菜单项
                    List<WebElement> menuItems = driver.findElements(
                            By.xpath("//*[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" +
                                    searchText.toLowerCase() + "')]")
                    );

                    for (WebElement item : menuItems) {
                        if (item.isDisplayed() && item.isEnabled()) {
                            actions.moveToElement(item).click().perform();
                            System.out.println("点击了 " + type + " 菜单项");
                            Thread.sleep(1500);
                            return true;
                        }
                    }
                } catch (Exception e) {
                    // 继续尝试其他方法
                }
            }

            // 使用JavaScript查找和点击
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String script = "var items = document.querySelectorAll('a, button, li, div');" +
                    "for(var i=0; i<items.length; i++) {" +
                    "  var item = items[i];" +
                    "  var text = item.innerText || item.textContent || '';" +
                    "  if(text.toLowerCase().includes('" + type.toLowerCase() + "')) {" +
                    "    item.click();" +
                    "    return true;" +
                    "  }" +
                    "}" +
                    "return false;";

            Boolean clicked = (Boolean) js.executeScript(script);
            if (clicked) {
                System.out.println("通过JavaScript点击了 " + type + " 菜单项");
                Thread.sleep(1500);
                return true;
            }

        } catch (Exception e) {
            System.err.println("点击菜单项时出错: " + e.getMessage());
        }

        return false;
    }

    /**
     * 等待文件下载完成
     */
    private void waitForFileDownload(String type, String league, int year) {
        String expectedFilename = type + ".json";
        String fullFilename = league + "_" + year + "_" + expectedFilename;
        File targetFile = new File(downloadDir, fullFilename);

        // 等待文件下载完成（最多60秒）
        long endTime = System.currentTimeMillis() + 60000;
        while (System.currentTimeMillis() < endTime) {
            if (targetFile.exists()) {
                System.out.println("✅ 已下载: " + fullFilename);
                return;
            }

            // 检查是否有.tmp文件（正在下载）
            File[] tmpFiles = new File(downloadDir).listFiles((dir, name) ->
                    name.contains(type) && (name.endsWith(".tmp") || name.endsWith(".crdownload"))
            );

            if (tmpFiles == null || tmpFiles.length == 0) {
                // 没有临时文件，可能下载完成或从未开始
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                System.out.println("正在下载中...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (!targetFile.exists()) {
            System.err.println("⚠️ 下载超时: " + fullFilename);
        }
    }

    /**
     * 直接下载文件（备用方案）
     */
    private void directDownload(String league, int year, String type) {
        try {
            String url = String.format("https://understat.com/league/%s/%d/%s.json", league, year, type);
            System.out.println("直接下载URL: " + url);

            // 使用JavaScript触发下载
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String script = String.format(
                    "var link = document.createElement('a');" +
                            "link.href = '%s';" +
                            "link.download = '%s_%d_%s.json';" +
                            "document.body.appendChild(link);" +
                            "link.click();" +
                            "document.body.removeChild(link);" +
                            "return true;",
                    url, league, year, type
            );

            js.executeScript(script);

            // 等待下载
            waitForFileDownload(type, league, year);

        } catch (Exception e) {
            System.err.println("直接下载失败: " + e.getMessage());
        }
    }

    /**
     * 获取已下载的文件列表
     */
    public List<String> getDownloadedFiles() {
        File dir = new File(downloadDir);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));

        List<String> fileList = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                fileList.add(file.getName());
            }
        }

        return fileList;
    }

    /**
     * 关闭浏览器
     */
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * 测试主函数
     */
    public static void main(String[] args) {
        // 配置下载目录
        String downloadDir = System.getProperty("user.home") + "/Downloads/understat_data";
        File dir = new File(downloadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        UnderstatSeleniumExporter exporter = null;

        try {
            // 创建导出器
            exporter = new UnderstatSeleniumExporter(downloadDir);

            // 配置要导出的联赛和赛季
            String league = "EPL";  // 可选的联赛: EPL, La_Liga, Serie_A, Bundesliga, Ligue_1, RFPL
            int year = 2025;

            System.out.println("开始导出 " + league + " " + year + " 赛季数据...");
            System.out.println("下载目录: " + downloadDir);

            // 执行导出
            exporter.exportLeagueData(league, year);

            // 显示已下载的文件
            System.out.println("\n已下载的文件:");
            List<String> files = exporter.getDownloadedFiles();
            for (String file : files) {
                System.out.println("  - " + file);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (exporter != null) {
                exporter.close();
            }
        }
    }
}