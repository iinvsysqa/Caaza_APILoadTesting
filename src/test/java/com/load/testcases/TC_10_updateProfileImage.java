package com.load.testcases;

import org.testng.annotations.Test;

import com.api.utils.reporter.ExtentReport;
import com.testcases.Product.C_CreateChildHierarchy;
import com.testcases.Product.B_CreateHeaderHierarchy;
import com.testcases.Product.E_serialEntry;
import com.testcases.Product.G_UpdateDevicename_switchesname_scenes;
import com.testcases.Product.I_Getandsavescenes;
import com.testcases.Product.J_updateProfileImage;

import wrappers.API_WrapperClass;
import wrappers.Baseclass;

public class TC_10_updateProfileImage extends Baseclass{
	
	
	public TC_10_updateProfileImage(String baseURL) {
		super(baseURL);
		// TODO Auto-generated constructor stub
	}
	J_updateProfileImage updateprofileimg;
@Test(priority = 9)
public void updateProfileImage() {

	updateprofileimg= new J_updateProfileImage();
	String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
ExtentReport.createdescription(methodName, "<b><I><h2>Sending request to serialEntry </h2></I></b>");


updateprofileimg.uploadProfileImageAndSaveUrl();
}

}
