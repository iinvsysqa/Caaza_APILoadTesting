package com.load.testcases;

import org.testng.annotations.Test;

import com.api.utils.reporter.ExtentReport;
import com.testcases.Product.C_CreateChildHierarchy;
import com.testcases.Product.B_CreateHeaderHierarchy;
import com.testcases.Product.E_serialEntry;
import com.testcases.Product.G_UpdateDevicename_switchesname_scenes;

import wrappers.API_WrapperClass;
import wrappers.Baseclass;

public class TC_07_updatedevicename_switchname_scenes extends Baseclass{
public TC_07_updatedevicename_switchname_scenes(String baseURL) {
		super(baseURL);
		// TODO Auto-generated constructor stub
	}

G_UpdateDevicename_switchesname_scenes update;
	
@Test(priority = 6)
public void updatedevicename_switchname_scenes() {
	update= new G_UpdateDevicename_switchesname_scenes(Integer.parseInt(loadProp("SCENECOUNT")));
	String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
ExtentReport.createdescription(methodName, "<b><I><h2>Sending request to serialEntry </h2></I></b>");

update.updateDeviceNameForUsers();
update.updateSwitchesForUsers();
update.createscene();


}

}
