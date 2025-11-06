package com.api.utils.reporter;

import java.awt.Font;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentReport{

    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    public static void initializeReport() {
        if (extent == null) {
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter("./extent-test-report/IHMS_API.html");
            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
        }
    }

//    public static void createTest(String testName) {
//        ExtentTest test = extent.createTest(testName);
//        extentTest.set(test);
//    }

    public static void createdescription(String testName,String font) {
    	
    	ExtentTest desc = extent.createTest(testName,font);
    	extentTest.set(desc);

	}
    public static ExtentTest getTest() {
        return extentTest.get();
    }

    public static void flushReport() {
        if (extent != null) {
            extent.flush();
        }
    }
}