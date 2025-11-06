package com.testcases.Product;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.api.utils.reporter.ExtentLogger;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import wrappers.Baseclass;

public class D_GetHierarchyLocationID extends Baseclass {

    private static final String BASE_URL = "https://ftp.iinvsys.com:55576";
    private static final String GET_TOP_HIERARCHY_ENDPOINT = "/core1/spl/users/get-top-hierarchy";
    private static final String FULL_DATA_CSV = "./output/AllUsersData.csv";
    private static final String LOCATION_OUTPUT_CSV = "./output/LocationIDs.csv";

    public D_GetHierarchyLocationID() {
        super(BASE_URL);
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    public void fetchTopHierarchyAndSaveToNewCSV() {
        List<String[]> locationRows = new ArrayList<>();

        // ✅ Add header for new CSV
        locationRows.add(new String[]{"Username", "Token", "Locationid"});

        try (CSVReader reader = new CSVReader(new FileReader(FULL_DATA_CSV))) {
            List<String[]> rows = reader.readAll();

            if (rows.isEmpty()) {
                ExtentLogger.fail("CSV file is empty: " + FULL_DATA_CSV);
                return;
            }

            String[] header = rows.get(0);
            int usernameIndex = findColumnIndex(header, "username");
            int tokenIndex = findColumnIndex(header, "token");

            if (usernameIndex == -1 || tokenIndex == -1) {
                ExtentLogger.fail("❌ 'username' or 'token' column not found in CSV.");
                return;
            }

            // ✅ Loop through users
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length <= Math.max(usernameIndex, tokenIndex)) continue;

                String username = row[usernameIndex].trim();
                String token = row[tokenIndex].replace("\"", "").trim();

                if (token.isEmpty()) {
                    ExtentLogger.info("⚠️ Skipping empty token for user: " + username);
                    continue;
                }

                try {
                    Response response = RestAssured.given()
                            .relaxedHTTPSValidation()
                            .header("Authorization", "Bearer " + token)
                            .get(GET_TOP_HIERARCHY_ENDPOINT);

                    if (response.statusCode() == 200) {
                        JSONObject jsonResponse = new JSONObject(response.getBody().asString());

                        if (jsonResponse.has("topHierarchy")) {
                            JSONArray hierarchyArray = jsonResponse.getJSONArray("topHierarchy");

                            if (hierarchyArray.isEmpty()) {
                                ExtentLogger.info("ℹ️ No location IDs found for " + username);
                                continue;
                            }

                            // 🔹 Add one row per location
                            for (int j = 0; j < hierarchyArray.length(); j++) {
                                JSONObject obj = hierarchyArray.getJSONObject(j);
                                if (obj.has("_id")) {
                                    String Location_ID = obj.getString("_id").trim();
                                    locationRows.add(new String[]{username, token, Location_ID});
                                }
                            }

                            ExtentLogger.pass("✅ Collected " + hierarchyArray.length() +
                                    " Location IDs for user: " + username);
                        } else {
                            ExtentLogger.info("❌ No topHierarchy found for: " + username);
                        }

                    } else {
                        ExtentLogger.info("❌ Request failed (" + response.statusCode() + ") for user: " + username);
                    }

                } catch (Exception e) {
                    ExtentLogger.fail("⚠️ Error fetching hierarchy for " + username + ": " + e.getMessage());
                }
            }

            // ✅ Write results to CSV
            try (CSVWriter writer = new CSVWriter(
                    new FileWriter(LOCATION_OUTPUT_CSV, false),
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END)) {

                writer.writeAll(locationRows);
            }

            ExtentLogger.pass("✅ Location IDs with Tokens saved successfully in: " + LOCATION_OUTPUT_CSV);

        } catch (IOException e) {
            ExtentLogger.fail("I/O error while processing CSV: " + e.getMessage());
        } catch (Exception e) {
            ExtentLogger.fail("Unexpected error: " + e.getMessage());
        }
    }

    // ✅ Utility: find column index by name
    private int findColumnIndex(String[] header, String columnName) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
}
