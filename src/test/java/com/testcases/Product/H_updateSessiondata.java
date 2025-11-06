package com.testcases.Product;

import java.io.FileReader;
import java.io.IOException;
import java.time.*;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.api.utils.reporter.ExtentLogger;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import wrappers.Baseclass;

public class H_updateSessiondata extends Baseclass {

    private static final String BASE_URL = "https://ftp.iinvsys.com:55576";
    private static final String USAGE_SESSION_UPDATE_ENDPOINT = "/analytical1/spl/usage_session_Update";
    private static final String FULL_DATA_CSV = "./output/AddedDevices.csv";

    public H_updateSessiondata() {
        super(BASE_URL);
        RestAssured.baseURI = BASE_URL;
    }

    // Utility: Read CSV file into a list of rows
    private List<String[]> readCsv(String path) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            return reader.readAll();
        }
    }

    
    public void updateSessionDataForAllDevices() {
        ExtentLogger.logLabel("🚀 Starting Session Data Upload for All Devices",
                com.aventstack.extentreports.markuputils.ExtentColor.BLUE);

        try {
            List<String[]> csvRows = readCsv(FULL_DATA_CSV);
            if (csvRows.size() <= 1) {
                ExtentLogger.fail("❌ CSV file has no user data");
                return;
            }

            int successCount = 0;
            String[] header = csvRows.get(0);

            int deviceIdIndex = findColumnIndex(header, "DeviceID");
            if (deviceIdIndex == -1) {
                ExtentLogger.fail("❌ Column 'DeviceID' not found in CSV");
                return;
            }

            // 🔹 Loop over all devices
            for (int i = 1; i < csvRows.size(); i++) {
                String[] row = csvRows.get(i);
                if (row.length <= deviceIdIndex) continue;

                String deviceId = row[deviceIdIndex].trim();
                if (deviceId.isEmpty()) {
                    ExtentLogger.info("⚠️ Skipping row " + (i + 1) + " — missing device_id");
                    continue;
                }

                // 🔹 Generate daily sessions for full year (2025)
                JSONArray sessionArray = new JSONArray();
                LocalDate startDate = LocalDate.of(2025, 1, 1);
                LocalDate endDate = LocalDate.of(2025, 12, 31);

                for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    long epochTime = date.atStartOfDay(ZoneOffset.UTC).toEpochSecond(); // midnight UTC
                    long durationSeconds = 7200; // 2 hours
                    int energyUsed = 767; // fixed for now

                    JSONObject sessionObj = new JSONObject();
                    sessionObj.put("swt_on_time", epochTime);
                    sessionObj.put("device_Id", deviceId);
                    sessionObj.put("swt_id", "1");
                    sessionObj.put("energy_used", energyUsed);
                    sessionObj.put("swt_on_off_dur", durationSeconds);

                    sessionArray.put(sessionObj);
                }

                JSONObject requestBody = new JSONObject();
                requestBody.put("sessions", sessionArray);

                try {
                    Response response = sendRequest("POST", USAGE_SESSION_UPDATE_ENDPOINT, requestBody, 200, null);
                    int status = response.getStatusCode();

                    if (status == 200) {
                        ExtentLogger.pass("✅ Session data updated for DeviceID: " + deviceId);
                        successCount++;
                    } else {
                        ExtentLogger.fail("❌ Failed for DeviceID " + deviceId + " | " + response.asString());
                    }

                } catch (Exception e) {
                    ExtentLogger.fail("⚠️ Exception updating session data for DeviceID " + deviceId + ": " + e.getMessage());
                }
            }

            ExtentLogger.logLabel("✅ Session update completed. Total devices updated: " + successCount,
                    com.aventstack.extentreports.markuputils.ExtentColor.GREEN);

        } catch (Exception e) {
            ExtentLogger.fail("❌ Error during execution: " + e.getMessage());
        }
    }

    // Utility: find index of a column by name
    private int findColumnIndex(String[] header, String columnName) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
}
