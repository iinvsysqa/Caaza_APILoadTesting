package wrappers;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.api.utils.reporter.ExtentLogger;
import com.api.utils.reporter.ExtentReport;

import java.lang.reflect.Method;

public class API_WrapperClass extends ExtentLogger {

    @BeforeSuite
    public void beforeSuite() {
        ExtentReport.initializeReport();
    }

    @BeforeMethod
    public void beforeMethod(Method method) {
        // Get the test method name
//        String testMethodName = method.getName();
        
        // Create the ExtentTest instance
//        ExtentReport.createTest(testMethodName);
        
        // Log the start of the test execution
//        ExtentLogger.info(testMethodName + " Test execution started");
    }

    @AfterMethod
    public void tearDown() {
        ExtentReport.flushReport();
    }
}