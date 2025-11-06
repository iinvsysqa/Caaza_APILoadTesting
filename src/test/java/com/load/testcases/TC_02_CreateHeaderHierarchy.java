package com.load.testcases;

import org.testng.annotations.Test;

import com.api.utils.reporter.ExtentReport;
import com.testcases.Product.C_CreateChildHierarchy;
import com.testcases.Product.B_CreateHeaderHierarchy;

import wrappers.API_WrapperClass;

public class TC_02_CreateHeaderHierarchy extends API_WrapperClass{
B_CreateHeaderHierarchy createhierarchy;
	

@Test(priority = 2)
public void CreateHeader_Hierarchy() {
	createhierarchy = new B_CreateHeaderHierarchy(10);
String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
ExtentReport.createdescription(methodName, "<b><I><h2>Sending request to  CreateHeaderHierarchy </h2></I></b>");

createhierarchy.createHierarchy();
    
}

}
