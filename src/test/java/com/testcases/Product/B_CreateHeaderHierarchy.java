package com.testcases.Product;

import com.api.utils.reporter.ExtentLogger;
import com.aventstack.extentreports.markuputils.ExtentColor;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import wrappers.Baseclass;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class B_CreateHeaderHierarchy extends Baseclass {

    // Constants
    private static final String CREATE_HIERARCHY_ENDPOINT = "/core1/spl/users/add-hierarchy-level";
    private static final String BASE_URL = "https://ftp.iinvsys.com:55576";
    private static final String FULL_DATA_CSV = "./output/AllUsersData.csv";

    private final int userCount;

    public B_CreateHeaderHierarchy(int count) {
        super(BASE_URL);
        this.userCount = count;
        RestAssured.baseURI = BASE_URL;
    }

    // Method to create hierarchy
    public void createHierarchy() {
        ExtentLogger.logLabel("Starting creation of hierarchy", ExtentColor.BLUE);

        // Read tokens using BufferedReader (same as F_Adddevice)
        List<String> tokens = readTokensFromCSV(FULL_DATA_CSV, userCount);
        if (tokens.isEmpty()) {
            ExtentLogger.fail("No tokens found in CSV file. Nothing to create.");
            return;
        }

        int successCount = 0;

        // Prepare the JSON body for hierarchy creation
        JSONObject requestBody = new JSONObject();
        requestBody.put("hierarchy_level", new JSONArray().put("Room"));

        for (String token : tokens) {
            // Send request using the base class method
            Response response = sendRequest("POST", CREATE_HIERARCHY_ENDPOINT, requestBody, 201, token);
            int statusCode = response.getStatusCode();
            String message = response.asPrettyString();

            ExtentLogger.info("Token: " + token.substring(0, Math.min(20, token.length())) + "... | Status Code: " + statusCode);
            ExtentLogger.info("Response: " + message);

            switch (statusCode) {
                case 200:
                case 201: // Accept both 200 and 201
                    ExtentLogger.pass("Hierarchy created successfully.");
                    successCount++;
                    break;
                case 400:
                    ExtentLogger.fail("Bad request - Check the request body: " + message);
                    break;
                case 401:
                    ExtentLogger.fail("Unauthorized - Token invalid or expired.");
                    break;
                default:
                    ExtentLogger.fail("Unexpected status code: " + statusCode + " | Message: " + message);
            }
        }

        ExtentLogger.logLabel("Hierarchy Creation Completed. Total created: " + successCount, ExtentColor.GREEN);
    }

    // ------------------------------------------------------------------------
    // HELPER: Read tokens from CSV using BufferedReader (same as F_Adddevice)
    // ------------------------------------------------------------------------
    private List<String> readTokensFromCSV(String csvPath, int maxCount) {
        List<String> tokens = new ArrayList<>();
        File file = new File(csvPath);

        if (!file.exists()) {
            ExtentLogger.fail("CSV file not found: " + csvPath);
            return tokens;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;
            int tokenIndex = -1;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] columns = splitCsvLine(line);

                // Detect header and find Token column
                if (isFirstLine) {
                    tokenIndex = indexOf(columns, "Token");
                    if (tokenIndex == -1) {
                        ExtentLogger.fail("Token column not found in CSV header!");
                        return tokens;
                    }
                    isFirstLine = false;
                    continue;
                }

                // Extract token from data row
                if (columns.length > tokenIndex) {
                    String token = columns[tokenIndex].replace("\"", "").trim();
                    if (!token.isEmpty()) {
                        tokens.add(token);
                        if (tokens.size() >= maxCount) {
                            break; // Stop after reading maxCount tokens
                        }
                    }
                }
            }

            ExtentLogger.info("Loaded " + tokens.size() + " token(s) from " + csvPath);
        } catch (IOException e) {
            ExtentLogger.fail("Error reading CSV: " + e.getMessage());
        }

        return tokens;
    }

    // ------------------------------------------------------------------------
    // Reuse your existing CSV split logic (same as F_Adddevice)
    // ------------------------------------------------------------------------
    private String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    private int indexOf(String[] arr, String key) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].trim().equalsIgnoreCase(key)) return i;
        }
        return -1;
    }
}