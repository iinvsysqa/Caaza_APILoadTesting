package com.api.utils.reporter;

import org.json.JSONObject;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;

import io.restassured.specification.RequestSpecification;
import wrappers.API_WrapperClass;

public class ExtentLogger {

    // Log a plain string message
    public static void info(String message) {
        ExtentReport.getTest().info(message);
    }

    // Log a Markup object (e.g., tables, code blocks, labels)
    public static void info(Markup markup) {
    	ExtentReport.getTest().info(markup);
    }

    public static void pass(String message) {
    	ExtentReport.getTest().pass(message);
    }

    public static void fail(String message) {
    	ExtentReport.getTest().fail(message);
    }

    // Log API request
    public static void logRequest(String request) {
        info(MarkupHelper.createCodeBlock("API Request: " + request));
    }
    
    public static void logRequestDetails(String baseURL, String endpoint, JSONObject requestBody, RequestSpecification request) {
        StringBuilder requestLog = new StringBuilder();
        requestLog.append("API Request Details:\n");

        // Log the full URL
        requestLog.append("URL: ").append(baseURL).append(endpoint).append("\n");

        // Log the headers
        requestLog.append("Headers: ").append(request.get().getHeaders().toString()).append("\n");

        // Log the request body
        if (requestBody != null) {
            requestLog.append("Body: ").append(requestBody.toString()).append("\n");
        }

        // Log the request in Extent Report
        info(MarkupHelper.createCodeBlock(requestLog.toString()));
    }
//    public static void logRequest(String baseURL, String endpoint, RequestSpecification request) {
//        StringBuilder requestLog = new StringBuilder();
//        requestLog.append("API Request Details:\n");
//
//        // Log full URL
//        requestLog.append("URL: ").append(baseURL).append(endpoint).append("\n");
//
//        // Log headers
//        requestLog.append("Headers: ").append(request.get().getHeaders()).append("\n");
//
//        // Log request body
//        if (request.get().body() != null) {
//            requestLog.append("Body: ").append(request.get().body().toString()).append("\n");
//        }
//
//        // Log the request details in the Extent Report
//        info(MarkupHelper.createCodeBlock(requestLog.toString()));
//    }

    // Log API response
    public static void logResponse(String response) {
        info(MarkupHelper.createCodeBlock("API Response: " + response));
        
    }

    // Log a label (e.g., for highlighting important information)
    public static void logLabel(String label, ExtentColor color) {
        info(MarkupHelper.createLabel(label, color));
    }
    
  
}