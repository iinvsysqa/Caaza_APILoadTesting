package com.load.testcases;

import org.testng.annotations.Test;

import com.api.utils.reporter.ExtentReport;
import com.testcases.Product.C_CreateChildHierarchy;
import com.testcases.Product.B_CreateHeaderHierarchy;
import com.testcases.Product.E_serialEntry;
import com.testcases.Product.G_UpdateDevicename_switchesname_scenes;
import com.testcases.Product.I_Getandsavescenes;

import wrappers.API_WrapperClass;
import wrappers.Baseclass;

public class TC_09_sceneupdate extends Baseclass{
public TC_09_sceneupdate(String baseURL) {
		super(baseURL);
		// TODO Auto-generated constructor stub
	}

G_UpdateDevicename_switchesname_scenes update;
	
@Test(priority = 8)
public void sceneupdate() {
	I_Getandsavescenes getscene;
	String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
ExtentReport.createdescription(methodName, "<b><I><h2>Sending request to serialEntry </h2></I></b>");


getscene= new I_Getandsavescenes();
getscene.fetchAndSaveSceneIds();

}

}
