package com.load.testcases;

import org.testng.annotations.Test;

import com.api.utils.reporter.ExtentReport;
import com.testcases.Product.F_Adddevice;
import com.testcases.Product.C_CreateChildHierarchy;
import com.testcases.Product.B_CreateHeaderHierarchy;
import com.testcases.Product.E_serialEntry;

import wrappers.API_WrapperClass;

public class TC_06_Add_device extends API_WrapperClass{

	F_Adddevice adddevice;
@Test(priority = 6)
public void Add_Device() throws Exception {
String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
ExtentReport.createdescription(methodName, "<b><I><h2>Sending request to Add device </h2></I></b>");

adddevice = new F_Adddevice(1);

adddevice.addDevicesForEachLocation();
}

}
