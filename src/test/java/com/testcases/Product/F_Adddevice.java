package com.testcases.Product;

import com.api.utils.reporter.ExtentLogger;
import com.aventstack.extentreports.markuputils.ExtentColor;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;
import wrappers.Baseclass;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class F_Adddevice extends Baseclass {

    private static final String ADD_DEVICE_ENDPOINT = "/core1/spl/devices/addDevice";
    private static final String LOCATION_CSV      = "./output/LocationIDs.csv";
    private static final String OUTPUT_CSV        = "./output/AddedDevices.csv";
    private static final String SERIAL_CSV        = "./output/serialnumbers.csv";

    private final int devicesPerLocation;
    private final List<String> serialNumbers = new ArrayList<>();
    private int serialIdx = 1;
    private int bleMacCounter = 1;               // for unique BLE_mac

    public F_Adddevice(int count) {
        super("https://ftp.iinvsys.com:55576");
        this.devicesPerLocation=count;
        RestAssured.baseURI = "https://ftp.iinvsys.com:55576";
    }

    public void addDevicesForEachLocation() {
        ExtentLogger.logLabel("Starting Device Creation for Each Location", ExtentColor.BLUE);

        try {
            loadSerialNumbers();                     // 1. load serials
            List<String[]> locationRows = readCsv(LOCATION_CSV); // 2. read locations

            if (locationRows.isEmpty() || locationRows.size() <= 1) {
                ExtentLogger.fail("No data in LocationIDs.csv!");
                return;
            }

            // ---- prepare output file ----
            try (BufferedWriter out = new BufferedWriter(new FileWriter(OUTPUT_CSV))) {
                out.write("Username,LocationID,Token,DeviceID,SerialNumber");
                out.newLine();

                // ---- find column indexes ----
                String[] header = locationRows.get(0);
                int userIdx   = indexOf(header, "Username");
                int tokenIdx  = indexOf(header, "Token");
                int locIdx    = indexOf(header, "LocationID");

                if (userIdx == -1 || tokenIdx == -1 || locIdx == -1) {
                    ExtentLogger.fail("Missing required columns in LocationIDs.csv!");
                    return;
                }

                int totalCreated = 0;

                // ---- process each location row (skip header) ----
                for (int i = 1; i < locationRows.size(); i++) {
                    String[] row = locationRows.get(i);
                    String username   = row[userIdx].trim();
                    String token      = row[tokenIdx].replace("\"", "").trim();
                    String locationId = row[locIdx].replace("\"", "").trim();

                    if (token.isEmpty() || locationId.isEmpty()) {
                        ExtentLogger.info("Skipping empty token/location for " + username);
                        continue;
                    }

                    ExtentLogger.info("Adding " + devicesPerLocation + " devices for " + username + " | Loc: " + locationId);

                    for (int d = 0; d < devicesPerLocation; d++) {
                        String serialNo = nextSerial();
                        if (serialNo == null) {
                            ExtentLogger.fail("Serial numbers exhausted!");
                            ExtentLogger.logLabel("Stopped – not enough serials.", ExtentColor.RED);
                            return;
                        }

                        String bleMac = generateBleMac();

                        JSONObject body = new JSONObject();
                        body.put("BLE_mac", bleMac);
                        body.put("build_number", 21);
                        body.put("cur_firm_vrs", "0.2");
                        body.put("hw_vrs", "0.3");
                        body.put("is_admin", true);
                        body.put("latitude", 11.9392385);
                        body.put("longitude", 79.8083949);
                        body.put("location_id", locationId);
                        body.put("region", "India");
                        body.put("router_mac", "CA:3B:AA:9A:30:EB");
                        body.put("router_ssid", "OPPO A9 2020");
                        body.put("serial_no", serialNo);
                        body.put("svr_ota", false);
                        body.put("timezone", "Asia/Kolkata");

                        try {
                        	Response resp = RestAssured.given()
                        	        .relaxedHTTPSValidation()  // Add this
                        	        .header("Authorization", "Bearer " + token)
                        	        .header("Content-Type", "application/json")
                        	        .body(body.toString())
                        	        .post(ADD_DEVICE_ENDPOINT);

                            if (resp.getStatusCode() == 200) {
                                JSONObject json = new JSONObject(resp.asString());
                                
                                String deviceId = "";
                                if (json.has("data") && json.getJSONObject("data").has("device_Id")) {
                                    deviceId = json.getJSONObject("data").getString("device_Id");
                                }

                                if (!deviceId.isEmpty()) {
                                    out.write(String.join(",", username, locationId, token, deviceId, serialNo));
                                    out.newLine();
                                    totalCreated++;
                                    ExtentLogger.pass("Device Added | Serial: " + serialNo +
                                            " | Loc: " + locationId + " | DevID: " + deviceId);
                                } else {
                                    ExtentLogger.fail("device_Id missing in 'data' for serial: " + serialNo 
                                            + " | Response: " + resp.asString());
                                }
                            } else {
                                ExtentLogger.fail("API error " + resp.getStatusCode() +
                                        " | Loc: " + locationId + " | Resp: " + resp.asString());
                            }
                        } catch (Exception ex) {
                            ExtentLogger.fail("Exception for " + username + " | Serial " + serialNo +
                                    " | " + ex.getMessage());
                        }
                    }
                }

                ExtentLogger.logLabel("Device creation complete – " + totalCreated + " added.", ExtentColor.GREEN);
            }

        } catch (IOException e) {
            ExtentLogger.fail("IO error: " + e.getMessage());
        }
    }

    /* --------------------------------------------------------------
       Helper: load serialnumbers.csv (plain text, one per line)
       -------------------------------------------------------------- */
    private void loadSerialNumbers() throws IOException {
        serialNumbers.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(SERIAL_CSV))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first && line.equalsIgnoreCase("SerialNumber")) {   // skip header
                    first = false;
                    continue;
                }
                serialNumbers.add(line);
                first = false;
            }
        }
        ExtentLogger.info("Loaded " + serialNumbers.size() + " serial numbers.");
        if (serialNumbers.isEmpty()) throw new IOException("serialnumbers.csv is empty");
    }

    private String nextSerial() {
        return (serialIdx < serialNumbers.size()) ? serialNumbers.get(serialIdx++) : null;
    }

    private String generateBleMac() {
        String hex = String.format("%02X", (bleMacCounter++ % 256));
        return "AA:BB:CC:" + hex + ":DD:EE";
    }

    /* --------------------------------------------------------------
       Helper: read any CSV into List<String[]>
       -------------------------------------------------------------- */
    private List<String[]> readCsv(String path) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                rows.add(splitCsvLine(line));
            }
        }
        return rows;
    }

    private String[] splitCsvLine(String line) {
        // simple split that respects values inside quotes
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