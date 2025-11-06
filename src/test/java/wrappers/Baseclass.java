package wrappers;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;

import com.api.utils.reporter.ExtentLogger;
import static org.testng.Assert.assertEquals;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.resources.*;

public class Baseclass extends API_WrapperClass {

    private static String baseURL;
    
    
    
    public static String admin_Username ="admin3" ;
    public static String admin_personalNumber ="54321" ;
    public static String admin_password ="1234";
    public static String DGset_id;
    public static String Pumps_id;
    public static String Compressor_id;
    
    public static String operator_personalNumber="111111";
    public static String operatorUsername="Karthi";
    public static String operator_Password="12345";
    

    public Baseclass(String baseURL) {
        this.baseURL = baseURL;
    }

    public Font font(String text) {
    	Font f= new Font(text, Font.PLAIN, 24);
    	return f;
	}
    
    // Register API call without token
    public  Response registerAPI(String endpoint, JSONObject requestBody) {
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        if (requestBody != null) {
            request.body(requestBody.toString());
        }
//        request.body(requestBody.toString());

        ExtentLogger.logRequestDetails(baseURL,endpoint,requestBody,request);
        return request.post(baseURL + endpoint);
    }

    
    public Response sendRequest(String method, String endpoint, JSONObject requestBody, 
		            int expectedStatusCode, String token) {

		
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json")
		.header("Authorization", "Bearer " + token);
		
		if (requestBody != null) {
		request.body(requestBody.toString());
		}
		
		ExtentLogger.logRequestDetails(baseURL, endpoint, requestBody, request);
		
		Response response = executeRequest(method, request, endpoint);
		checkStatusCode(response.statusCode(), expectedStatusCode);
		return response;
		}

    // API call with query parameters
//    public Response sendRequestWithParams(String token,String method, String endpoint, JSONObject requestBody, int expectedStatusCode, String... queryParams) {
//        RequestSpecification request = RestAssured.given();
//        request.header("Content-Type", "application/json");
//        request.header("Authorization", "Bearer " + token);
//        request.header("Notification-Token", notificationToken);
//
//        // Add query parameters
//        if (queryParams != null && queryParams.length % 2 == 0) {
//            for (int i = 0; i < queryParams.length; i += 2) {
//                request.queryParam(queryParams[i], queryParams[i + 1]);
//            }
//        }
//
//        if (requestBody != null) {
//            request.body(requestBody.toString());
//        }
//
//        Response response = executeRequest(method, request, endpoint);
//        assertEquals(response.statusCode(), expectedStatusCode, "Status Code does not match!");
//        return response;
//    }

    // Execute the request based on the method
    private Response executeRequest(String method, RequestSpecification request, String endpoint) {
        switch (method.toUpperCase()) {
            case "POST":
                return request.post(baseURL + endpoint);
            case "PUT":
                return request.put(baseURL + endpoint);
            case "GET":
                return request.get(baseURL + endpoint);
            case "DELETE":
                return request.delete(baseURL + endpoint);
            default:
                throw new IllegalArgumentException("Invalid method: " + method);
        }
    }

    public  void checkStatusCode(int statusCode, int expectedStatusCode) {
        if (statusCode != expectedStatusCode) {
            ExtentLogger.fail(statusCode + " actual status code is not equal to " + expectedStatusCode);
        } else {
            ExtentLogger.pass("API Status code is " + expectedStatusCode);
        }
    }
    
    public  void checkResponseMessage(String response ,String expected) {

    	String string = response.toString();
    	
    	if (string.contains(expected)) {
    		ExtentLogger.pass(string +"response matches the expected response message" + expected);
        } else {
        	ExtentLogger.fail( string+ "response not matches the expected response message" + expected);
        }    	
	}
    public Response delete(String endpoint, Map<String, String> headers) {
        Response response = null;
        try {
            RequestSpecification request = RestAssured.given().relaxedHTTPSValidation();

            if (headers != null) request.headers(headers);

            ExtentLogger.logRequestDetails(RestAssured.baseURI, endpoint, null, request);

            response = request.delete(endpoint);

            ExtentLogger.logResponse(response.asPrettyString());
            ExtentLogger.pass("DELETE request executed successfully with status code: " + response.getStatusCode());
        } catch (Exception e) {
            ExtentLogger.fail("DELETE request failed: " + e.getMessage());
        }
        return response;
    }
    public static String loadProp(String property) {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(new File("./config.properties")));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop.getProperty(property);
	}
	public static void updateProperty( String key, String newValue) throws IOException {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("./config.properties")) {
            props.load(in);
        }

        // Update the value
        props.setProperty(key, newValue);

        try (FileOutputStream out = new FileOutputStream("./config.properties")) {
            props.store(out, null);
        }
    }

}