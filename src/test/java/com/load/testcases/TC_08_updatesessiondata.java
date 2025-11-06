package com.load.testcases;

import org.testng.annotations.Test;

import com.api.utils.reporter.ExtentReport;
import com.testcases.Product.C_CreateChildHierarchy;
import com.testcases.Product.B_CreateHeaderHierarchy;
import com.testcases.Product.E_serialEntry;
import com.testcases.Product.G_UpdateDevicename_switchesname_scenes;
import com.testcases.Product.H_updateSessiondata;

import wrappers.API_WrapperClass;
import wrappers.Baseclass;

public class TC_08_updatesessiondata extends Baseclass{
public TC_08_updatesessiondata(String baseURL) {
		super(baseURL);
		// TODO Auto-generated constructor stub
	}

H_updateSessiondata updatesession;
	
@Test(priority = 7)
public void updatesessiondata() {
	updatesession= new H_updateSessiondata();	
	String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
ExtentReport.createdescription(methodName, "<b><I><h2>Sending request to serialEntry </h2></I></b>");

updatesession.updateSessionDataForAllDevices();

}

}
