package com.testcases.Product;

import java.io.FileReader;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.api.utils.reporter.ExtentLogger;
import com.opencsv.CSVReader;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import wrappers.Baseclass;

public class E_serialEntry extends Baseclass {

    // Constants
    private static final String SERIAL_ENTRY_ENDPOINT = "core1/spl/devices/serial_Entry";
    private static final String BASE_URL = "https://ftp.iinvsys.com:55576";
    private static final String SERIALNO_CSV = "./output/serialnumbers.csv";

    public E_serialEntry() {
        super(BASE_URL);
        RestAssured.baseURI = BASE_URL;
    }

    // Method to post serial numbers from CSV
    public void postSerialNumbersFromCSV() {
        ExtentLogger.logLabel("🚀 Starting Serial Number Upload", 
            com.aventstack.extentreports.markuputils.ExtentColor.BLUE);

        try (CSVReader reader = new CSVReader(new FileReader(SERIALNO_CSV))) {
            List<String[]> rows = reader.readAll();

            if (rows.size() <= 1) {
                ExtentLogger.fail("❌ CSV file is empty or has only header: " + SERIALNO_CSV);
                return;
            }

            int successCount = 0;
            int totalCount = 0;

            // Skip header (start from index 1)
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);

                if (row.length == 0 || row[0].trim().isEmpty()) {
                    ExtentLogger.info("⚠️ Skipping empty serial number at row " + (i + 1));
                    continue;
                }

                String serialNo = row[0].trim();
                totalCount++;

                // Create body for each serial number
                JSONObject requestBody = new JSONObject();
                JSONArray serialArray = new JSONArray();
                JSONObject serialObj = new JSONObject();
                serialObj.put("serial_no", serialNo);
                serialArray.put(serialObj);
                requestBody.put("serial_no_data", serialArray);

                try {
                    // Send POST request
                    Response response = RestAssured.given()
                            .relaxedHTTPSValidation()
                            .header("Content-Type", "application/json")
                            .body(requestBody.toString())
                            .post(SERIAL_ENTRY_ENDPOINT);

                    int statusCode = response.statusCode();

                    ExtentLogger.info("📦 Serial No: " + serialNo + " | Status Code: " + statusCode);

                    if (statusCode == 200) {
                        ExtentLogger.pass("✅ Serial entry successful for: " + serialNo);
                        successCount++;
                    } else {
                        ExtentLogger.fail("❌ Failed for serial: " + serialNo + 
                                          " | Response: " + response.asPrettyString());
                    }

                } catch (Exception e) {
                    ExtentLogger.fail("⚠️ Error posting serial: " + serialNo + " | " + e.getMessage());
                }
            }

            ExtentLogger.logLabel("✅ Serial Entry Completed. Total: " + totalCount + 
                                  " | Success: " + successCount, 
                                  com.aventstack.extentreports.markuputils.ExtentColor.GREEN);

        } catch (Exception e) {
            ExtentLogger.fail("❌ Error reading CSV or sending requests: " + e.getMessage());
        }
    }
}
