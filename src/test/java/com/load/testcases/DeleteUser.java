package com.load.testcases;

import org.testng.annotations.Test;

import com.api.utils.reporter.ExtentReport;
import com.testcases.Product.A_RegisterUser;
import com.testcases.Product.Z_DeleteUser_working;

import wrappers.API_WrapperClass;

public class DeleteUser extends API_WrapperClass{

	
Z_DeleteUser_working delete;

//@Test(priority = 3)
public void DeleteUser() {

	delete= new Z_DeleteUser_working(1);
String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
ExtentReport.createdescription(methodName, "<b><I><h2>Sending request to delete users</h2></I></b>");

 delete.DeletUsers();    
    
}

}
