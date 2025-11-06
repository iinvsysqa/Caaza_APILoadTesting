package com.load.testcases;

import org.testng.annotations.Test;

import com.api.utils.reporter.ExtentReport;
import com.testcases.Product.C_CreateChildHierarchy;

import wrappers.API_WrapperClass;

public class TC_03_Createchildhierarchy extends API_WrapperClass{
C_CreateChildHierarchy createhierarchy;
	

@Test(priority = 3)
public void CreatechildHierarchy() {
	createhierarchy = new C_CreateChildHierarchy(10,10);
String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
ExtentReport.createdescription(methodName, "<b><I><h2>Sending request to  CreatechildHierarchy</h2></I></b>");

createhierarchy.createHierarchy();
    
}

}
