package com.testcases.Product;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Test;

import com.api.utils.reporter.ExtentLogger;
import com.api.utils.reporter.ExtentReport;
import com.load.testcases.TC_01_AddUser;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.opencsv.CSVWriter;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import io.restassured.response.Response;
import wrappers.Baseclass;

public class A_RegisterUser extends Baseclass {


	  public A_RegisterUser(int count) {
		  super(BASE_URL);
	        this.userCount = count;
	        RestAssured.baseURI = BASE_URL;
	    }
	
	
	
	

	

	    private static final String REGISTER_ENDPOINT = "/core1/spl/users/user_registeration";
	    private static final String BASE_URL = "https://ftp.iinvsys.com:55576";
	    private static final String FULL_DATA_CSV = "./output/AllUsersData.csv";
	    private static final String LOGIN_CREDENTIALS_CSV = "./output/UserCredentials.csv";

	    private int userCount; // Number of users to register

	    

	    // Generate random user JSON
	    private JSONObject generateRandomUser() {
	        String randomId = UUID.randomUUID().toString().substring(0, 6);
	        String name = "User_" + randomId;
	        String username = "user_" + randomId.toLowerCase();
	        String password = "Welcome@123";

	        JSONObject user = new JSONObject();
	        user.put("name", name);
	        user.put("username", username);
	        user.put("password", password);
	        user.put("user_role", "user");

	        JSONArray questions = new JSONArray();
	        questions.put(new JSONObject(Map.of("qn", "what is your name", "ans", "mode")));
	        questions.put(new JSONObject(Map.of("qn", "who is albert", "ans", "mode")));
	        user.put("security_questions", questions);

	        return user;
	    }

	    // Save to CSV file
	    public static void saveToCSV(String filePath, List<String[]> data) {
	        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath, true))) {
	            writer.writeAll(data);
	        } catch (IOException e) {
	            System.out.println("⚠️ Error writing to CSV: " + e.getMessage());
	        }
	    }

	    // Main registration logic
	    public void registerUsers() throws Exception {
	        ExtentLogger.logLabel("Starting User Registration for " + userCount + " users", com.aventstack.extentreports.markuputils.ExtentColor.BLUE);

	        
	        clearOrCreateCSV(FULL_DATA_CSV);
	        clearOrCreateCSV(LOGIN_CREDENTIALS_CSV);
	        
	        // Initialize CSV with headers
	        saveToCSV(FULL_DATA_CSV, Collections.singletonList(new String[]{"Name", "Username", "Password", "UserRole", "Token", "UserID", "Message"}));
	        saveToCSV(LOGIN_CREDENTIALS_CSV, Collections.singletonList(new String[]{"Username", "Password"}));

	        for (int i = 1; i <= userCount; i++) {
	            JSONObject userBody = generateRandomUser();

	            // Headers
	            Map<String, String> headers = new HashMap<>();
	            headers.put("Content-Type", "application/json");

	            // POST Request
	            Response response = registerAPI(REGISTER_ENDPOINT, userBody);
	            int statusCode = response.getStatusCode();

	            ExtentLogger.info("User " + i + " | Status Code: " + statusCode);
	            ExtentLogger.info("Response: " + response.asPrettyString());

	            // Handle response
	            String message;
	            String token = "";
	            String userId = "";

	            try {
	                message = response.jsonPath().getString("message");
	                if (statusCode == 200) {
	                    token = response.jsonPath().getString("token");
	                    userId = response.jsonPath().getString("user_id");

	                    ExtentLogger.pass("✅ User Registered: " + userBody.getString("username"));

	                    // Save to both CSV files
	                    saveToCSV(FULL_DATA_CSV, Collections.singletonList(new String[]{
	                            userBody.getString("name"),
	                            userBody.getString("username"),
	                            userBody.getString("password"),
	                            userBody.getString("user_role"),
	                            token,
	                            userId,
	                            message
	                    }));

	                    saveToCSV(LOGIN_CREDENTIALS_CSV, Collections.singletonList(new String[]{
	                            userBody.getString("username"),
	                            userBody.getString("password")
	                    }));

	                } else if (statusCode == 400) {
	                    ExtentLogger.fail("❌ 400 Validation Error: " + message);
	                } else {
	                    ExtentLogger.fail("⚠️ Unexpected status: " + statusCode + " | Message: " + message);
	                }

	            } catch (Exception e) {
	                ExtentLogger.fail("Error parsing response: " + e.getMessage());
	            }
	        }

	        ExtentLogger.logLabel("User Registration Completed", com.aventstack.extentreports.markuputils.ExtentColor.GREEN);
	    }

	    private void clearOrCreateCSV(String filePath) throws Exception {
	        File file = new File(filePath);
	        try {
	            if (file.exists()) {
	                // Delete the old file
	                file.delete();
	                Thread.sleep(8000);
	            }
	            // Create a new file
	            file.createNewFile();
	            Thread.sleep(8000);
	        } catch (IOException e) {
	            System.out.println("⚠️ Error creating CSV file: " + e.getMessage());
	        }
	    }
	}
	

