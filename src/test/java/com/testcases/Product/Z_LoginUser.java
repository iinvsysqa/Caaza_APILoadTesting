package com.testcases.Product;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import com.api.utils.reporter.ExtentLogger;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import wrappers.Baseclass;

public class Z_LoginUser extends Baseclass {

    private static final String LOGIN_ENDPOINT = "/core1/spl/users/login";
    private static final String BASE_URL = "https://ftp.iinvsys.com:55576";
    private static final String FULL_DATA_CSV = "./output/AllUsersData.csv";
    private static final String LOGIN_CREDENTIALS_CSV = "./output/UserCredentials.csv";

    private int userCount;
    public Z_LoginUser(int count) {
    	
        super(BASE_URL);
        this.userCount=count;
        RestAssured.baseURI = BASE_URL;
    }

    // Method to read login credentials from CSV
    private List<String[]> readLoginCredentials() {
        List<String[]> credentials = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(LOGIN_CREDENTIALS_CSV));
            for (String line : lines) {
                String[] data = line.split(",");
                credentials.add(data);
            }
        } catch (IOException e) {
            ExtentLogger.fail("Error reading login credentials: " + e.getMessage());
        }
        return credentials;
    }


    
    private void updateTokenInCSV(String username, String token) throws CsvException {
        List<String[]> allLines = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(FULL_DATA_CSV))) {
            List<String[]> rows = reader.readAll();

            String cleanToken = token.replace("\"", "").trim();

            for (String[] data : rows) {
                // Assuming username in column 1 (index 1)
                if (data.length > 1 && data[1].replace("\"", "").equals(username)) {
                    data[4] = cleanToken;
                }
                allLines.add(data);
            }

            try (CSVWriter writer = new CSVWriter(
                    new FileWriter(FULL_DATA_CSV, false),
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,  // No quotes around cells
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END)) {

                writer.writeAll(allLines);
            }

            ExtentLogger.pass("✅ Token updated for user: " + username);

        } catch (IOException e) {
            ExtentLogger.fail("❌ Error updating tokens in CSV: " + e.getMessage());
        }
    }



    // Main login logic
    public void loginUsers() throws CsvException {
        ExtentLogger.logLabel("Starting User Login for " + userCount + " users", com.aventstack.extentreports.markuputils.ExtentColor.BLUE);

        List<String[]> loginCredentials = readLoginCredentials();
        
        if (loginCredentials.isEmpty()) {
            ExtentLogger.fail("❌ No login credentials found in CSV file.");
            return;
        }

        for (String[] creds : loginCredentials) {
            String username = creds[0]; // Assuming username is in the first column
            String password = creds[1]; // Assuming password is in the second column

            username = username.replace("\"", "");
            password = password.replace("\"", "");
            // Create JSON body for login
            JSONObject loginBody = new JSONObject();
            loginBody.put("username", username);
            loginBody.put("password", password);

            System.out.println("Username"+" "+username+"Password"+"   "+password);
            // Headers
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");


            
            Response response = registerAPI(LOGIN_ENDPOINT, loginBody);

            int statusCode = response.getStatusCode();
            String message = response.asPrettyString();

            ExtentLogger.info("Login attempt for " + username + " | Status Code: " + statusCode);
            ExtentLogger.info("Response: " + message);

            if (statusCode == 200) {
                String token = response.jsonPath().getString("token");
                ExtentLogger.pass("✅ User Logged In: " + username + " | Token: " + token);
                updateTokenInCSV(username, token);
            } else {
                ExtentLogger.fail("❌ Login failed for " + username + " | Message: " + message);
            }
        }

        ExtentLogger.logLabel("User Login Completed", com.aventstack.extentreports.markuputils.ExtentColor.GREEN);
    }
}