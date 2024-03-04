package framework.modules;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.aventstack.extentreports.reporter.configuration.ViewName;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class testController {
    public static ExtentReports reports = new ExtentReports();
    public static ExtentSparkReporter spark;
    public static ExtentTest logger;
    public static WebDriverWait wait;
    public static WebElement element;
    static WebDriver driver;
    Properties pros = new Properties();
    @Parameters({"browserName", "isHeadless"})
    @BeforeTest
    public void setup(String browserName, boolean isHeadless) {
        driver = openBrowser(browserName, isHeadless);
        spark = sparkSetup();
        reports.setSystemInfo("Environment", "QA");
        reports.attachReporter(spark);
    }
    @BeforeMethod
    public void register(Method method) {
        String testName = method.getName();
        logger = reports.createTest(testName);
    }
    @AfterMethod
    public void captureStatus(ITestResult result) throws Exception {
        if (result.getStatus() == ITestResult.SUCCESS) {
            logger.log(Status.PASS, "The test method name as : " + result.getTestName() + " is passed");
        } else if (result.getStatus() == ITestResult.FAILURE) {
            logger.log(Status.FAIL, "The test method name as : " + result.getTestName() + " is failed");
            logger.log(Status.FAIL, "The test case failed due to : " + result.getThrowable());
            String screenshotPath = testController.getScreenshot(driver, result.getTestName());
            logger.addScreenCaptureFromPath(screenshotPath);
        } else if (result.getStatus() == ITestResult.SKIP) {
            logger.log(Status.SKIP, "The test method name as : " + result.getTestName() + " has been skipped");
        } else logger.log(Status.INFO, "N/A");
    }

    @AfterTest(alwaysRun = true)
    public void cleanUp(){
        reports.flush();
        driver.close();
        driver.quit();
    }
    private static ExtentSparkReporter sparkSetup(){
        spark = new ExtentSparkReporter(new File(System.getProperty("user.dir") + "/reports/Automation_report-" + System.currentTimeMillis() + ".html")).viewConfigurer()
                .viewOrder()
                .as(new ViewName[]{
                        ViewName.DASHBOARD,
                        ViewName.TEST,
                        ViewName.EXCEPTION,
                        ViewName.LOG
                })
                .apply();
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setDocumentTitle("Automation Report" + System.currentTimeMillis());
        spark.config().setReportName("SMOKE DEMO");
        return spark;
    }
    public static String getScreenshot(WebDriver driver, String screenshotName) throws Exception {
        //below line is just to append the date format with the screenshot name to avoid duplicate names
        String dateName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        TakesScreenshot ts = (TakesScreenshot) driver;
        File source = ts.getScreenshotAs(OutputType.FILE);
        //after execution, you could see a folder "FailedTestsScreenshots" under src folder
        String destination = System.getProperty("user.dir") + "/reports/FailedTestsScreenshots/"+screenshotName+dateName+".png";
        File finalDestination = new File(destination);
        FileUtils.copyFile(source, finalDestination);
        //Returns the captured file path
        return destination;
    }
    public static WebDriver openBrowser(String browserName, boolean isHeadless) {
        if (browserName.equalsIgnoreCase("chrome")) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("window-size=1920,1080");
            options.addArguments("--remote-allow-origins=*");
            if (isHeadless){
                options.addArguments("--Headless");
            }
            return new ChromeDriver(options);
        } else if (browserName.equalsIgnoreCase("firefox")) {
            WebDriverManager.firefoxdriver().setup();
            FirefoxBinary firefoxBinary = new FirefoxBinary();
            FirefoxOptions options = new FirefoxOptions();
            options.setBinary(firefoxBinary);
            if (isHeadless){
                options.setHeadless(true);
            }
            return new FirefoxDriver(options);
        } else if (browserName.equalsIgnoreCase("edge")) {
            WebDriverManager.edgedriver().setup();
            EdgeOptions options = new EdgeOptions();
            options.addArguments("window-size=1920,1080");
            options.addArguments("--remote-allow-origins=*");
            if (isHeadless){
                options.addArguments("--Headless");
            }
            return new EdgeDriver(options);
        } else throw new IllegalArgumentException("The Browser " + browserName + " does not support");
    }
    protected static Object[][] getDataFromFile(Sheet excelSheet) {
        int rowCOUNT = excelSheet.getLastRowNum() - excelSheet.getFirstRowNum();
        Object[][] object = new Object[rowCOUNT][2];
        for (int i = 0; i < rowCOUNT; i++) {
            Row row = excelSheet.getRow(i + 1);
            for (int j = 0; j < row.getLastCellNum(); j++) {
                object[i][j] = row.getCell(j).toString();
            }
        }
        return object;
    }
}
