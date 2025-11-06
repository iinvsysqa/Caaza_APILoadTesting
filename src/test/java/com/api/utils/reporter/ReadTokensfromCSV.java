package com.api.utils.reporter;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


public class ReadTokensfromCSV {

	public List<String> readTokensFromCSV(String filePath,int count) {
	    List<String> tokens = new ArrayList<>();
	    try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
	        String[] nextLine;
	        boolean skipHeader = true;
	        while ((nextLine = reader.readNext()) != null) {
	            if (skipHeader) { 
	                skipHeader = false; 
	                continue; 
	            }
	            if (nextLine.length > count && nextLine[count] != null && !nextLine[count].isEmpty()) {
	                tokens.add(nextLine[4].trim()); // 5th column → Token
	            }
	        }
	    } catch (Exception e) {
	        ExtentLogger.fail("⚠️ Error reading tokens from CSV: " + e.getMessage());
	    }
	    return tokens;
	}

}
