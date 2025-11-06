package com.load.testcases;

import org.testng.annotations.Test;

import com.api.utils.reporter.ExtentReport;
import com.testcases.Product.C_CreateChildHierarchy;

import wrappers.API_WrapperClass;
import wrappers.Baseclass;

public class TC_03_Createchildhierarchy extends Baseclass{
public TC_03_Createchildhierarchy(String baseURL) {
		super(baseURL);
		// TODO Auto-generated constructor stub
	}


C_CreateChildHierarchy createhierarchy;
	

@Test(priority = 2)
public void CreatechildHierarchy() {
	createhierarchy = new C_CreateChildHierarchy(Integer.parseInt(loadProp("USERS")),Integer.parseInt(loadProp("ROOMCOUNT")));
	
	
String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
ExtentReport.createdescription(methodName, "<b><I><h2>Sending request to  CreatechildHierarchy</h2></I></b>");

createhierarchy.createHierarchy();
    
}

}
