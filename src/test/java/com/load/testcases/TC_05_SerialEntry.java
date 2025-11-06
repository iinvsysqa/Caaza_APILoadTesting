package com.load.testcases;

import org.testng.annotations.Test;

import com.api.utils.reporter.ExtentReport;
import com.testcases.Product.C_CreateChildHierarchy;
import com.testcases.Product.B_CreateHeaderHierarchy;
import com.testcases.Product.E_serialEntry;

import wrappers.API_WrapperClass;

public class TC_05_SerialEntry extends API_WrapperClass{
E_serialEntry serialentry;	

@Test(priority = 5)
public void serialEntry() {
	serialentry= new E_serialEntry();
String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
ExtentReport.createdescription(methodName, "<b><I><h2>Sending request to serialEntry </h2></I></b>");

    serialentry.postSerialNumbersFromCSV();
}

}
