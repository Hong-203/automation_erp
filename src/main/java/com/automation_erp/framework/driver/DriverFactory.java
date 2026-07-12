package com.automation_erp.framework.driver;

import com.automation_erp.framework.config.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory khởi tạo WebDriver (hỗ trợ chạy Local và Remote WebDriver cho Selenium Grid / BrowserStack).
 */
public class DriverFactory {

    public static WebDriver createDriverInstance() {
        WebDriver driver;
        String browserName = ConfigReader.getBrowser().toLowerCase();
        boolean headless = ConfigReader.isHeadless();
        boolean isRemote = Boolean.parseBoolean(ConfigReader.getProperty("remote.web.driver"));

        if (isRemote) {
            driver = createRemoteDriver(browserName, headless);
        } else {
            driver = createLocalDriver(browserName, headless);
        }

        driver.manage().window().maximize();
        return driver;
    }

    private static WebDriver createLocalDriver(String browserName, boolean headless) {
        switch (browserName) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--remote-allow-origins=*");
                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                }
                return new ChromeDriver(chromeOptions);

            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("-headless");
                }
                return new FirefoxDriver(firefoxOptions);

            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                if (headless) {
                    edgeOptions.addArguments("--headless");
                }
                return new EdgeDriver(edgeOptions);

            default:
                throw new IllegalArgumentException("Unsupported local browser: " + browserName);
        }
    }

    private static WebDriver createRemoteDriver(String browserName, boolean headless) {
        String gridUrl = ConfigReader.getProperty("selenium.grid.url");
        String bsUsername = ConfigReader.getProperty("browserstack.username");
        String bsKey = ConfigReader.getProperty("browserstack.key");
        
        URL remoteUrl;
        try {
            // Nếu có cấu hình BrowserStack username/key, sử dụng hub BrowserStack
            if (bsUsername != null && !bsUsername.trim().isEmpty() && !"YOUR_BROWSERSTACK_USERNAME".equals(bsUsername)) {
                remoteUrl = new URL(String.format("https://%s:%s@hub-cloud.browserstack.com/wd/hub", bsUsername.trim(), bsKey.trim()));
            } else {
                remoteUrl = new URL(gridUrl);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Đường dẫn Remote WebDriver URL không hợp lệ: " + e.getMessage(), e);
        }

        System.out.println("[DriverFactory] Đang khởi tạo Remote WebDriver kết nối tới: " + remoteUrl);

        switch (browserName) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--remote-allow-origins=*");
                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                }
                if (bsUsername != null && !bsUsername.trim().isEmpty()) {
                    Map<String, Object> bstackOptions = new HashMap<>();
                    bstackOptions.put("os", "Windows");
                    bstackOptions.put("osVersion", "11");
                    chromeOptions.setCapability("bstack:options", bstackOptions);
                }
                return new RemoteWebDriver(remoteUrl, chromeOptions);

            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("-headless");
                }
                return new RemoteWebDriver(remoteUrl, firefoxOptions);

            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                if (headless) {
                    edgeOptions.addArguments("--headless");
                }
                return new RemoteWebDriver(remoteUrl, edgeOptions);

            default:
                throw new IllegalArgumentException("Unsupported remote browser: " + browserName);
        }
    }
}
