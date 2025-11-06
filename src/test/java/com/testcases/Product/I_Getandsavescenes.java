package com.testcases.Product;

import com.api.utils.reporter.ExtentLogger;
import com.aventstack.extentreports.markuputils.ExtentColor;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import wrappers.Baseclass;

import java.io.*;
import java.util.*;

public class I_Getandsavescenes extends Baseclass {

    private static final String GET_SCENES_ENDPOINT = "/core1/spl/users/get-all-scenes";
    private static final String LOCATION_CSV = "./output/LocationIDs.csv";
    private static final String OUTPUT_CSV = "./output/AddedDevices.csv";

    public I_Getandsavescenes() {
        super("https://ftp.iinvsys.com:55576");
        RestAssured.baseURI = "https://ftp.iinvsys.com:55576";
    }

    public void fetchAndSaveSceneIds() {
        ExtentLogger.logLabel("Fetching & Saving Scene IDs to AddedDevices.csv", ExtentColor.BLUE);

        try {
            List<String[]> locationRows = readCsv(LOCATION_CSV);
            if (locationRows.size() <= 1) {
                ExtentLogger.fail("No data in LocationIDs.csv!");
                return;
            }

            String[] header = locationRows.get(0);
            int tokenIdx = indexOf(header, "Token");
            int userIdx = indexOf(header, "Username");
            int locIdx = indexOf(header, "LocationID");

            if (tokenIdx == -1 || locIdx == -1) {
                ExtentLogger.fail("Missing Token or LocationID in LocationIDs.csv");
                return;
            }

            Set<String> processedTokens = new LinkedHashSet<>();
            Map<String, String> tokenToSceneId = new HashMap<>();

            // Step 1: Fetch Scene ID for each unique token
            for (int i = 1; i < locationRows.size(); i++) {
                String[] row = locationRows.get(i);
                String token = row[tokenIdx].replace("\"", "").trim();
                String locationId = row[locIdx].replace("\"", "").trim();
                String username = userIdx != -1 ? row[userIdx].trim() : "N/A";

                if (token.isEmpty() || locationId.isEmpty()) continue;
                if (processedTokens.contains(token)) continue;

                ExtentLogger.info("Fetching scenes for user: " + username + " | Location: " + locationId);

                try {
                    Response resp = RestAssured.given()
                            .relaxedHTTPSValidation()
                            .header("Authorization", "Bearer " + token)
                            .get(GET_SCENES_ENDPOINT);

                    if (resp.getStatusCode() == 200) {
                        JsonPath jp = resp.jsonPath();
                        List<String> sceneIds = jp.getList("scenes._id");
                        if (sceneIds != null && !sceneIds.isEmpty()) {
                            String sceneId = sceneIds.get(0); // Take first scene
                            tokenToSceneId.put(token, sceneId);
                            processedTokens.add(token);
                            ExtentLogger.pass("Scene ID fetched: " + sceneId + " for token ending ... " + token.substring(token.length() - 6));
                        } else {
                            ExtentLogger.fail("No scenes found for token: " + maskToken(token));
                        }
                    } else {
                        ExtentLogger.fail("API failed: " + resp.getStatusCode() + " | " + resp.asString());
                    }
                } catch (Exception e) {
                    ExtentLogger.fail("Exception: " + e.getMessage());
                }
            }

            // Step 2: Update AddedDevices.csv with SceneID in the same row
            updateAddedDevicesWithSceneId(tokenToSceneId);

            ExtentLogger.logLabel("Scene IDs saved successfully to AddedDevices.csv", ExtentColor.GREEN);

        } catch (IOException e) {
            ExtentLogger.fail("IO Error: " + e.getMessage());
        }
    }

    private void updateAddedDevicesWithSceneId(Map<String, String> tokenToSceneId) throws IOException {
        File inputFile = new File(OUTPUT_CSV);
        if (!inputFile.exists()) {
            ExtentLogger.fail(OUTPUT_CSV + " not found!");
            return;
        }

        List<String[]> rows = readCsv(OUTPUT_CSV);
        if (rows.isEmpty()) return;

        String[] header = rows.get(0);
        List<String> headerList = new ArrayList<>(Arrays.asList(header));
        int tokenColIdx = headerList.indexOf("Token");
        int sceneColIdx = headerList.indexOf("SceneID");

        // Add SceneID column if not exists
        if (sceneColIdx == -1) {
            headerList.add("SceneID");
            sceneColIdx = headerList.size() - 1;
        }

        // Update rows
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row.length <= tokenColIdx) continue;

            String rowToken = row[tokenColIdx].replace("\"", "").trim();
            String sceneId = tokenToSceneId.getOrDefault(rowToken, "");

            // Resize row if needed
            if (row.length <= sceneColIdx) {
                row = Arrays.copyOf(row, sceneColIdx + 1);
                rows.set(i, row);
            }
            row[sceneColIdx] = sceneId;
        }

        // Write back safely
        File tempFile = new File(OUTPUT_CSV + ".tmp");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
            // Write header
            bw.write(String.join(",", headerList));
            bw.newLine();

            // Write rows
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                String[] fullRow = new String[headerList.size()];
                System.arraycopy(row, 0, fullRow, 0, Math.min(row.length, fullRow.length));
                for (int j = row.length; j < fullRow.length; j++) {
                    fullRow[j] = "";
                }
                bw.write(String.join(",", fullRow));
                bw.newLine();
            }
        }

        // Replace original
        if (inputFile.delete() && tempFile.renameTo(inputFile)) {
            ExtentLogger.info("AddedDevices.csv updated with SceneID column");
        } else {
            ExtentLogger.fail("Failed to update AddedDevices.csv");
        }
    }

    // Helper methods
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

    private int indexOf(List<String> list, String key) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).trim().equalsIgnoreCase(key)) return i;
        }
        return -1;
    }

    private int indexOf(String[] arr, String key) {
        return indexOf(Arrays.asList(arr), key);
    }

    private String maskToken(String token) {
        return token.length() > 8 ? token.substring(0, 4) + "..." + token.substring(token.length() - 4) : token;
    }
}