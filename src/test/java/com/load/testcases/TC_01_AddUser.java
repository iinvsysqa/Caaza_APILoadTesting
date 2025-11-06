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

import org.testng.ITestContext;
import org.testng.annotations.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import com.resources.TestDataManager;
import com.testcases.Product.A_RegisterUser;

//import com.resources.TokenManager;
import java.util.List;



// To do - Access denied - need to give other user token and get that response so do that after login operator and use his token 
public class TC_01_AddUser extends API_WrapperClass{

			A_RegisterUser register;
	    
  
    @Test(priority = 0)
    public void Add_User() throws Exception {
    	
    	register =new A_RegisterUser(10);
    	String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    	ExtentReport.createdescription(methodName, "<b><I><h2>Sending request without role</h2></I></b>");
    	
    	register.registerUsers();
    	
            
    }

   
}