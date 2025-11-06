package com.testcases.Product;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import com.api.utils.reporter.ExtentLogger;
import com.api.utils.reporter.ReadTokensfromCSV;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import wrappers.Baseclass;

public class Z_DeleteUser_working  extends Baseclass{




	  public Z_DeleteUser_working(int count) {
		  super(BASE_URL);
	        this.userCount = count;
	        RestAssured.baseURI = BASE_URL;
	    }
	
	ReadTokensfromCSV readTokensfromCSV = new ReadTokensfromCSV();


	    private static final String REGISTER_ENDPOINT = "/core1/spl/users/delete-account";
	    private static final String BASE_URL = "https://ftp.iinvsys.com:55576";
	    private static final String FULL_DATA_CSV = "./output/AllUsersData.csv";
	    private static final String LOGIN_CREDENTIALS_CSV = "./output/UserCredentials.csv";

	    private int userCount; // Number of users to register

	    



	    
	    public void DeletUsers() {
	        ExtentLogger.logLabel("Starting deletion of signed-up users", com.aventstack.extentreports.markuputils.ExtentColor.BLUE);

	        // Read tokens from CSV
	        List<String> tokens = readTokensfromCSV.readTokensFromCSV(FULL_DATA_CSV,userCount);

	        if (tokens.isEmpty()) {
	            ExtentLogger.fail("❌ No tokens found in CSV file. Nothing to delete.");
	            return;
	        }

	        int successCount = 0;

	        for (String token : tokens) {
	            Map<String, String> headers = new HashMap<>();
	            headers.put("Content-Type", "application/json");
	            headers.put("Authorization", "Bearer " + token);

	            // DELETE Request using your wrapper
	            Response response = delete(REGISTER_ENDPOINT, headers);

	            int statusCode = response.getStatusCode();
	            String message = response.asPrettyString();

	            ExtentLogger.info("Token: " + token.substring(0, Math.min(20, token.length())) + "... | Status Code: " + statusCode);
	            ExtentLogger.info("Response: " + message);

	            switch (statusCode) {
	                case 200:
	                    ExtentLogger.pass("✅ User deleted successfully.");
	                    successCount++;
	                    break;
	                case 210:
	                    ExtentLogger.fail("⚠️ Conditional delete message: " + message);
	                    break;
	                case 401:
	                    ExtentLogger.fail("❌ Unauthorized - Token invalid or expired.");
	                    break;
	                default:
	                    ExtentLogger.fail("⚠️ Unexpected status code: " + statusCode + " | Message: " + message);
	            }
	        }

	        ExtentLogger.logLabel("User Deletion Completed. Total deleted: " + successCount, com.aventstack.extentreports.markuputils.ExtentColor.GREEN);
	    }



	
}
