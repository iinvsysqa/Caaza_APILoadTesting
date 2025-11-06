package com.resources;

import java.util.HashMap;
import java.util.Map;
public class TestDataManager {


	    private static final Map<String, Object> testData = new HashMap<>();
	    
	    private TestDataManager() {}
	    
	    public static synchronized void storeData(String key, Object value) {
	        testData.put(key, value);
	    }
	    
	    public static synchronized Object getData(String key) {
	        if (!testData.containsKey(key)) {
	            throw new IllegalArgumentException("Key " + key + " not found in test data");
	        }
	        return testData.get(key);
	    }
	    
	    public static synchronized String getStringData(String key) {
	        return (String) getData(key);
	    }
	    
	    public static synchronized void clearData() {
	        testData.clear();
	    }
	}

