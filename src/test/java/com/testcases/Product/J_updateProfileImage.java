package com.testcases.Product;

import com.api.utils.reporter.ExtentLogger;
import com.aventstack.extentreports.markuputils.ExtentColor;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import wrappers.Baseclass;

import java.io.*;
import java.util.*;

public class J_updateProfileImage extends Baseclass {

    private static final String BASE_URL = "https://ftp.iinvsys.com:55576";
    private static final String UPDATE_PROFILE_ENDPOINT = "/core1/spl/users/update-user";
    private static final String CSV_PATH = "./output/AllUsersData.csv";
    private static final String IMAGE_PATH = "C:\\Users\\Invcuser_71\\Pictures\\reactlogo.png";

    public J_updateProfileImage() {
        super(BASE_URL);
        RestAssured.baseURI = BASE_URL;
    }

    public void uploadProfileImageAndSaveUrl() {
        ExtentLogger.logLabel("Uploading Profile Image & Saving URL to CSV", ExtentColor.BLUE);

        File imageFile = new File(IMAGE_PATH);
        if (!imageFile.exists()) {
            ExtentLogger.fail("Image file not found: " + IMAGE_PATH);
            return;
        }

        try {
            List<String[]> csvData = readCsv(CSV_PATH);
            if (csvData.isEmpty() || csvData.size() <= 1) {
                ExtentLogger.fail("CSV is empty or has no data rows: " + CSV_PATH);
                return;
            }

            String[] header = csvData.get(0);
            int tokenIdx      = findColumnIndex(header, "Token");
            int profileImgIdx = findColumnIndex(header, "ProfileImg");   // may be -1

            if (tokenIdx == -1) {
                ExtentLogger.fail("Token column not found in CSV!");
                return;
            }

            // --------------------------------------------------------------
            // Add ProfileImg column if it does not exist yet
            // --------------------------------------------------------------
            boolean hasProfileImgColumn = profileImgIdx != -1;
            if (!hasProfileImgColumn) {
                header = Arrays.copyOf(header, header.length + 1);
                header[header.length - 1] = "ProfileImg";
                csvData.set(0, header);
            }

            int successCount = 0;
            int totalRows    = csvData.size() - 1;

            for (int i = 1; i < csvData.size(); i++) {
                String[] row   = csvData.get(i);
                String token   = row[tokenIdx].replace("\"", "").trim();

                if (token.isEmpty()) {
                    ExtentLogger.info("Skipping empty token at row " + i);
                    continue;
                }

                ExtentLogger.info("Uploading image for token: " + maskToken(token));

                try {
                    Response response = RestAssured.given()
                            .relaxedHTTPSValidation()
                            .header("Authorization", "Bearer " + token)
                            .multiPart("profile_img", imageFile)   // form-data field name
                            .put(UPDATE_PROFILE_ENDPOINT);

                    int status = response.getStatusCode();

                    if (status == 200) {
                        // ------------------------------------------------------
                        // RESPONSE STRUCTURE (as you posted):
                        // {
                        //   "status":"success",
                        //   "message":"...",
                        //   "user":{
                        //       "profile_img":"user_7807f81762413863197.png",
                        //       ...
                        //   }
                        // }
                        // ------------------------------------------------------
                        String profileImg = response.jsonPath().getString("user.profile_img");

                        if (profileImg == null || profileImg.trim().isEmpty()) {
                            ExtentLogger.fail("profile_img not found in response for token: " + maskToken(token));
                            continue;
                        }


                        // ------------------------------------------------------
                        // Update the row with the URL
                        // ------------------------------------------------------
                        if (!hasProfileImgColumn) {
                            row = Arrays.copyOf(row, row.length + 1);
                            csvData.set(i, row);
                        }
                        row[row.length - 1] = profileImg;

                        ExtentLogger.pass("Image uploaded. URL saved: " + profileImg);
                        successCount++;
                    } else {
                        ExtentLogger.fail(
                            "API failed [" + status + "] | " +
                            response.asString() + " | Token: " + maskToken(token)
                        );
                    }
                } catch (Exception e) {
                    ExtentLogger.fail("Exception for token " + maskToken(token) + ": " + e.getMessage());
                }
            }

            // --------------------------------------------------------------
            // Write the updated CSV back (overwrites the original file)
            // --------------------------------------------------------------
            writeCsv(csvData, CSV_PATH);

            ExtentLogger.logLabel(
                "Profile image upload complete. " + successCount + "/" + totalRows + " updated.",
                ExtentColor.GREEN
            );

        } catch (Exception e) {
            ExtentLogger.fail("Critical error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------------
    // CSV Helpers (same as your other classes)
    // ------------------------------------------------------------------------
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

    private void writeCsv(List<String[]> data, String path) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (String[] row : data) {
                bw.write(String.join(",", row));
                bw.newLine();
            }
        }
        ExtentLogger.info("CSV updated: " + path);
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

    private int findColumnIndex(String[] header, String columnName) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    private String maskToken(String token) {
        return token.length() > 8 ? token.substring(0, 4) + "..." + token.substring(token.length() - 4) : token;
    }
}