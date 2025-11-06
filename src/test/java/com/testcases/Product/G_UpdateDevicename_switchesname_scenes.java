package com.testcases.Product;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import com.api.utils.reporter.ExtentLogger;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import wrappers.Baseclass;

public class G_UpdateDevicename_switchesname_scenes extends Baseclass {

    private static final String BASE_URL = "https://ftp.iinvsys.com:55576";
    private static final String UPDATE_DEVICE_NAME_ENDPOINT = "/core1/spl/devices/updateDeviceName";
    private static final String UPDATE_SWITCHES_ENDPOINT = "/core1/spl/devices/addSwitches";
    private static final String CREATE_SCENE_ENDPOINT = "/core1/spl/users/scene";
    
    private static final String FULL_DATA_CSV = "./output/AddedDevices.csv";
    int SCENE_COUNT;
    private final Random random = new Random();

    public G_UpdateDevicename_switchesname_scenes(int SCENE_COUNT) {
        super(BASE_URL);
        this.SCENE_COUNT=SCENE_COUNT;
        RestAssured.baseURI = BASE_URL;
    }

    private List<String[]> readCsv(String path) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            return reader.readAll();
        }
    }

    private int findColumnIndex(String[] header, String columnName) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    private String generateDeviceName() {
        String[] prefixes = { "SmartPlug", "EnergyHub", "RoomController", "Device", "Node" };
        return prefixes[random.nextInt(prefixes.length)] + "_" + (1000 + random.nextInt(9000));
    }

    private JSONArray generateRandomSwitches() {
        String[][] switchOptions = {
                {"Light", "light.png", "light"},
                {"fan", "fan.png", "fan"},
                {"AC", "ac.png", "ac"},
                {"Ceiling fan", "ceiling fan.png", "ceiling fan"},
                {"dishwasher", "dishwasher.png", "dishwasher"},
                {"exhaustfan", "exhaustfan.png", "exhaust fan"},
                {"fridge", "fridge.png", "fridge"},
                {"iron box", "ironbox.png", "iron box"}
        };

        JSONArray switchesArray = new JSONArray();
        int numberOfSwitches = 3 ;

        for (int i = 0; i < numberOfSwitches; i++) {
            int idx = random.nextInt(switchOptions.length);
            JSONObject sw = new JSONObject();
            sw.put("switch_id", String.valueOf(i + 1));
            sw.put("switch_name", switchOptions[idx][0]);
            sw.put("switch_type", switchOptions[idx][2]);
            sw.put("switch_image", switchOptions[idx][1]);
            sw.put("enrg_id", "0");
            sw.put("src_name", random.nextBoolean());
            sw.put("timer_dur", 0);
            sw.put("set_timer", false);
            switchesArray.put(sw);
        }
        return switchesArray;
    }

    
    public void updateDeviceNameForUsers() {
        ExtentLogger.logLabel("🚀 Updating Device Names", com.aventstack.extentreports.markuputils.ExtentColor.BLUE);

        try {
            List<String[]> csvRows = readCsv(FULL_DATA_CSV);
            if (csvRows.size() <= 1) return;

            String[] header = csvRows.get(0);
            int tokenIndex = findColumnIndex(header, "Token");
            int deviceIdIndex = findColumnIndex(header, "DeviceID");

            for (int i = 1; i < csvRows.size(); i++) {
                String[] row = csvRows.get(i);
                if (row.length <= Math.max(tokenIndex, deviceIdIndex)) continue;

                String token = row[tokenIndex].trim();
                String deviceId = row[deviceIdIndex].trim();

                if (token.isEmpty() || deviceId.isEmpty()) continue;

                JSONObject body = new JSONObject();
                body.put("name", generateDeviceName());
                body.put("device_id", deviceId);

                Response res = sendRequest("POST", UPDATE_DEVICE_NAME_ENDPOINT, body, 200, token);
                if (res.getStatusCode() == 200) {
                    ExtentLogger.pass("✅ Device name updated: " + deviceId);
                } else {
                    ExtentLogger.fail("❌ Device name update failed for " + deviceId + " | " + res.asString());
                }
                
                
                
            }

        } catch (Exception e) {
            ExtentLogger.fail("❌ Error: " + e.getMessage());
        }
    }
    public void createscene() {
        final String SCENE_NAME_PREFIX = "Night Mode";

        ExtentLogger.logLabel(
            "Creating one scene per **unique token** (ignoring SCENE_COUNT for token-based creation)",
            com.aventstack.extentreports.markuputils.ExtentColor.BLUE
        );

        try {
            List<String[]> csvRows = readCsv(FULL_DATA_CSV);
            if (csvRows.size() <= 1) {
                ExtentLogger.info("CSV has no data rows. Skipping scene creation.");
                return;
            }

            String[] header = csvRows.get(0);
            int locationIdIndex = findColumnIndex(header, "LocationID");
            int deviceIdIndex   = findColumnIndex(header, "DeviceID");
            int tokenIndex      = findColumnIndex(header, "Token");

            if (locationIdIndex == -1 || deviceIdIndex == -1 || tokenIndex == -1) {
                ExtentLogger.fail("Required columns (LocationID, DeviceID, Token) not found in CSV.");
                return;
            }

            /* --------------------------------------------------------------
               1. Group devices by **unique token** – duplicates are ignored
               -------------------------------------------------------------- */
            Map<String, List<DeviceEntry>> tokenToDevices = new LinkedHashMap<>();

            for (int i = 1; i < csvRows.size(); i++) {
                String[] row = csvRows.get(i);
                if (row.length <= Math.max(Math.max(locationIdIndex, deviceIdIndex), tokenIndex)) continue;

                String locationId = row[locationIdIndex].trim();
                String deviceId   = row[deviceIdIndex].trim();
                String token      = row[tokenIndex].trim();

                if (locationId.isEmpty() || deviceId.isEmpty() || token.isEmpty()) continue;

                tokenToDevices
                    .computeIfAbsent(token, k -> new ArrayList<>())
                    .add(new DeviceEntry(deviceId, token, locationId));
            }

            if (tokenToDevices.isEmpty()) {
                ExtentLogger.info("No valid token-device pairs found in CSV.");
                return;
            }

            /* --------------------------------------------------------------
               2. Create **one scene for every unique token**
               -------------------------------------------------------------- */
            int sceneNumber = 1;
            for (Map.Entry<String, List<DeviceEntry>> entry : tokenToDevices.entrySet()) {
                String token      = entry.getKey();
                List<DeviceEntry> devices = entry.getValue();
                String locationId = devices.get(0).locationId; // same for all devices of this token

                String sceneName = SCENE_NAME_PREFIX + (tokenToDevices.size() > 1 ? " #" + sceneNumber : "");

                // Build JSON payload – one mapping per device
                JSONArray mappingsArray = new JSONArray();
                for (int d = 0; d < devices.size(); d++) {
                    DeviceEntry dev = devices.get(d);

                    JSONObject switchObj = new JSONObject();
                    switchObj.put("switch_id", String.valueOf(d + 1));
                    switchObj.put("desired_state", false);

                    JSONArray switchesArray = new JSONArray();
                    switchesArray.put(switchObj);

                    JSONObject mappingObj = new JSONObject();
                    mappingObj.put("location_id", locationId);
                    mappingObj.put("device_id",   dev.deviceId);
                    mappingObj.put("switches",    switchesArray);

                    mappingsArray.put(mappingObj);
                }

                JSONObject body = new JSONObject();
                body.put("scene_name", sceneName);
                body.put("scene_mappings", mappingsArray);

                // Send request
                Response res = sendRequest("POST", CREATE_SCENE_ENDPOINT, body, 200, token);

                if (res.getStatusCode() == 200) {
                    ExtentLogger.pass(
                        "Scene '" + sceneName + "' created for Token …" +
                        token.substring(token.length() - 6) +
                        " | Location: " + locationId +
                        " | Devices: " + devices.size()
                    );
                } else {
                    ExtentLogger.fail(
                        "Failed to create scene '" + sceneName + "' for Token …" +
                        token.substring(token.length() - 6) +
                        " | Status: " + res.getStatusCode() +
                        " | Response: " + res.asString()
                    );
                }
                sceneNumber++;
            }

            ExtentLogger.logLabel(
                "Scene creation complete. " + tokenToDevices.size() + " unique token(s) processed.",
                com.aventstack.extentreports.markuputils.ExtentColor.GREEN
            );

        } catch (Exception e) {
            ExtentLogger.fail("Error in createscene(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* -----------------------------------------------------------------
       Helper – now holds locationId too
       ----------------------------------------------------------------- */
    private static class DeviceEntry {
        String deviceId;
        String token;
        String locationId;

        DeviceEntry(String deviceId, String token, String locationId) {
            this.deviceId   = deviceId;
            this.token      = token;
            this.locationId = locationId;
        }
    }
    
//    public void createscene() {
//         // CHANGE THIS TO CONTROL HOW MANY SCENES TO CREATE
//        final String SCENE_NAME_PREFIX = "Night Mode";
//
//        ExtentLogger.logLabel(
//            "Creating " + SCENE_COUNT + " Scene(s) - One per Location",
//            com.aventstack.extentreports.markuputils.ExtentColor.BLUE
//        );
//
//        try {
//            List<String[]> csvRows = readCsv(FULL_DATA_CSV);
//            if (csvRows.size() <= 1) {
//                ExtentLogger.info("CSV has no data rows. Skipping scene creation.");
//                return;
//            }
//
//            String[] header = csvRows.get(0);
//            int locationIdIndex = findColumnIndex(header, "LocationID");
//            int deviceIdIndex   = findColumnIndex(header, "DeviceID");
//            int tokenIndex      = findColumnIndex(header, "Token");
//
//            if (locationIdIndex == -1 || deviceIdIndex == -1 || tokenIndex == -1) {
//                ExtentLogger.fail("Required columns (LocationID, DeviceID, Token) not found in CSV.");
//                return;
//            }
//
//            // -----------------------------------------------------------------
//            // 1. Group devices by LocationID
//            // -----------------------------------------------------------------
//            Map<String, List<DeviceEntry>> locationToDevices = new LinkedHashMap<>();
//
//            for (int i = 1; i < csvRows.size(); i++) {
//                String[] row = csvRows.get(i);
//                if (row.length <= Math.max(Math.max(locationIdIndex, deviceIdIndex), tokenIndex)) {
//                    continue;
//                }
//
//                String locationId = row[locationIdIndex].trim();
//                String deviceId   = row[deviceIdIndex].trim();
//                String token      = row[tokenIndex].trim();
//
//                if (locationId.isEmpty() || deviceId.isEmpty() || token.isEmpty()) {
//                    continue;
//                }
//
//                locationToDevices
//                    .computeIfAbsent(locationId, k -> new ArrayList<>())
//                    .add(new DeviceEntry(deviceId, token));
//            }
//
//            if (locationToDevices.isEmpty()) {
//                ExtentLogger.info("No valid location-device pairs found in CSV.");
//                return;
//            }
//
//            // -----------------------------------------------------------------
//            // 2. Take only SCENE_COUNT locations
//            // -----------------------------------------------------------------
//            List<String> selectedLocations = locationToDevices.keySet()
//                .stream()
//                .limit(SCENE_COUNT)
//                .collect(Collectors.toList());
//
//            if (selectedLocations.isEmpty()) {
//                ExtentLogger.info("SCENE_COUNT is 0 or no locations available.");
//                return;
//            }
//
//            int sceneNumber = 1;
//            for (String locationId : selectedLocations) {
//                List<DeviceEntry> devices = locationToDevices.get(locationId);
//                String sceneName = SCENE_NAME_PREFIX + (SCENE_COUNT > 1 ? " #" + sceneNumber : "");
//
//                // Use the token of the first device in the location (or pick any valid one)
//                String token = devices.get(0).token;
//
//                // -----------------------------------------------------------------
//                // 3. Build JSON: One scene with all devices in this location
//                // -----------------------------------------------------------------
//                JSONArray mappingsArray = new JSONArray();
//
//                for (int d = 0; d < devices.size(); d++) {
//                    DeviceEntry dev = devices.get(d);
//
//                    JSONObject switchObj = new JSONObject();
//                    switchObj.put("switch_id", String.valueOf(d + 1)); // or use actual switch ID from CSV
//                    switchObj.put("desired_state", false);
//
//                    JSONArray switchesArray = new JSONArray();
//                    switchesArray.put(switchObj);
//
//                    JSONObject mappingObj = new JSONObject();
//                    mappingObj.put("location_id", locationId);
//                    mappingObj.put("device_id", dev.deviceId);
//                    mappingObj.put("switches", switchesArray);
//
//                    mappingsArray.put(mappingObj);
//                }
//
//                JSONObject body = new JSONObject();
//                body.put("scene_name", sceneName);
//                body.put("scene_mappings", mappingsArray);
//
//                // -----------------------------------------------------------------
//                // 4. Send Request
//                // -----------------------------------------------------------------
//                Response res = sendRequest("POST", CREATE_SCENE_ENDPOINT, body, 200, token);
//
//                if (res.getStatusCode() == 200) {
//                    ExtentLogger.pass(
//                        "Scene '" + sceneName + "' created for Location: " + locationId +
//                        " | Devices: " + devices.size()
//                    );
//                } else {
//                    ExtentLogger.fail(
//                        "Failed to create scene '" + sceneName + "' for Location: " + locationId +
//                        " | Status: " + res.getStatusCode() +
//                        " | Response: " + res.asString()
//                    );
//                }
//
//                sceneNumber++;
//            }
//
//        } catch (Exception e) {
//            ExtentLogger.fail("Error in createscene(): " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

    // -----------------------------------------------------------------
    // Helper class to hold device + token
    // -----------------------------------------------------------------
//    private static class DeviceEntry {
//        String deviceId;
//        String token;
//
//        DeviceEntry(String deviceId, String token) {
//            this.deviceId = deviceId;
//            this.token = token;
//        }
//    }
    
    
    
    
    
    
  
    public void updateSwitchesForUsers() {
        ExtentLogger.logLabel("🚀 Adding Switches to Devices",
                com.aventstack.extentreports.markuputils.ExtentColor.BLUE);

        try {
            List<String[]> csvRows = readCsv(FULL_DATA_CSV);
            if (csvRows.size() <= 1) return;

            String[] header = csvRows.get(0);
            int tokenIndex = findColumnIndex(header, "token");
            int deviceIdIndex = findColumnIndex(header, "DeviceID");

            for (int i = 1; i < csvRows.size(); i++) {
                String[] row = csvRows.get(i);
                if (row.length <= Math.max(tokenIndex, deviceIdIndex)) continue;

                String token = row[tokenIndex].trim();
                String deviceId = row[deviceIdIndex].trim();

                if (token.isEmpty() || deviceId.isEmpty()) continue;

                JSONObject body = new JSONObject();
                body.put("device_id", deviceId);
                body.put("switches", generateRandomSwitches());
                body.put("node_type", "2");

                Response res = sendRequest("POST", UPDATE_SWITCHES_ENDPOINT, body, 200, token);
                if (res.getStatusCode() == 200) {
                    ExtentLogger.pass("✅ Switches added for Device: " + deviceId);
                } else {
                    ExtentLogger.fail("❌ Switch update failed for " + deviceId + " | " + res.asString());
                }
            }

        } catch (Exception e) {
            ExtentLogger.fail("❌ Error: " + e.getMessage());
        }
    }
}
