package com.testcases.Product;

import com.api.utils.reporter.ExtentLogger;
import com.aventstack.extentreports.markuputils.ExtentColor;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import wrappers.Baseclass;

import java.io.*;
import java.util.*;

public class C_CreateChildHierarchy extends Baseclass {

    private static final String BASE_URL = "https://ftp.iinvsys.com:55576";
    private static final String CREATE_HIERARCHY_ENDPOINT = "/core1/spl/users/add-hierarchy";
    private static final String FULL_DATA_CSV = "./output/AllUsersData.csv";

    private final int userCount;
    private final int roomCount;

    public C_CreateChildHierarchy(int userCount, int roomCount) {
        super(BASE_URL);
        this.userCount = userCount;
        this.roomCount = roomCount;
        RestAssured.baseURI = BASE_URL;
    }

    /**
     * Generate a random room name
     */
    private String generateRoomName() {
        String[] prefixes = {"bedroom", "livingroom", "kitchen", "hall", "studyroom", "office", "balcony"};
        String randomPrefix = prefixes[new Random().nextInt(prefixes.length)];
        String randomSuffix = UUID.randomUUID().toString().substring(0, 5);
        return randomPrefix + "_" + randomSuffix;
    }

    /**
     * Generate a hierarchy JSON body with given number of rooms
     */
    private JSONObject generateHierarchyBody(int roomCount) {
        JSONArray hierarchyArray = new JSONArray();
        Set<String> uniqueRoomNames = new HashSet<>();

        for (int i = 0; i < roomCount; i++) {
            String roomName = generateUniqueRoomName(uniqueRoomNames);

            JSONObject room = new JSONObject()
                    .put("name", roomName)
                    .put("type", "Room")
                    .put("catagory", "living room")
                    .put("catagory_image", "livingroom.jpg");

            hierarchyArray.put(room);
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("hierarchy", hierarchyArray);
        return requestBody;
    }

    private String generateUniqueRoomName(Set<String> existingNames) {
        String roomName;
        do {
            roomName = "Room_" + generateRandomString(4);
        } while (existingNames.contains(roomName));
        existingNames.add(roomName);
        return roomName;
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Main method to create hierarchies
     */
    public void createHierarchy() {
        ExtentLogger.logLabel("Starting creation of " + roomCount + " rooms per user", ExtentColor.BLUE);

        // Read tokens using BufferedReader
        List<String> tokens = readTokensFromCSV(FULL_DATA_CSV, userCount);
        if (tokens.isEmpty()) {
            ExtentLogger.fail("No tokens found in CSV file. Nothing to create.");
            return;
        }

        int totalSuccess = 0;
        int totalUsers = tokens.size();

        for (int i = 0; i < totalUsers; i++) {
            String token = tokens.get(i);
            JSONObject requestBody = generateHierarchyBody(roomCount);

            ExtentLogger.info("Creating " + roomCount + " rooms for user " + (i + 1));

            try {
                Response response = sendRequest("POST", CREATE_HIERARCHY_ENDPOINT, requestBody, 201, token);
                int statusCode = response.getStatusCode();

                if (statusCode == 200 || statusCode == 201) {
                    ExtentLogger.pass("Hierarchy created successfully for user " + (i + 1));
                    totalSuccess++;
                } else {
                    ExtentLogger.fail("Failed for user " + (i + 1) + " | Code: " + statusCode + " | " + response.asString());
                }
            } catch (Exception e) {
                ExtentLogger.fail("Exception for user " + (i + 1) + ": " + e.getMessage());
            }
        }

        ExtentLogger.logLabel("Completed! Total Users Processed: " + totalUsers +
                        " | Success: " + totalSuccess, ExtentColor.GREEN);
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
                            break;
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
    // Reuse CSV split logic (same as F_Adddevice)
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