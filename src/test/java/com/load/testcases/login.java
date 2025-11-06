package com.load.testcases;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import wrappers.API_WrapperClass;
import wrappers.Baseclass;

import org.json.JSONObject;
import org.junit.Rule;
import org.junit.rules.TestName;

import com.api.utils.reporter.ExtentLogger;
import com.api.utils.reporter.ExtentReport;
import com.opencsv.exceptions.CsvException;

import org.testng.ITestContext;
import org.testng.annotations.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import com.resources.TestDataManager;
import com.testcases.Product.A_RegisterUser;
import com.testcases.Product.Z_LoginUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
//import com.resources.TokenManager;
import java.util.List;



// To do - Access denied - need to give other user token and get that response so do that after login operator and use his token 
public class login extends API_WrapperClass{

			Z_LoginUser  login;
	    
  
    @Test(priority = 1)
    public void Login() throws IOException, CsvException {
    	
login = new Z_LoginUser(2);
    	String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    	ExtentReport.createdescription(methodName, "<b><I><h2>Sending request without role</h2></I></b>");
    	
    	cleanExistingCSV("./output/AllUsersData.csv");
    	login.loginUsers();
            
            
    }
    
    public static void cleanExistingCSV(String path) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path));
        List<String> cleaned = new ArrayList<>();

        for (String line : lines) {
            cleaned.add(line.replace("\"", "")); // remove all double quotes
        }

        Files.write(Paths.get(path), cleaned);
        System.out.println("✅ Cleaned all double quotes from file.");
    }


   
}