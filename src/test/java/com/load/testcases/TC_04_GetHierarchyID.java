package com.load.testcases;

import org.testng.annotations.Test;

import com.api.utils.reporter.ExtentReport;
import com.opencsv.exceptions.CsvException;
import com.testcases.Product.C_CreateChildHierarchy;
import com.testcases.Product.B_CreateHeaderHierarchy;
import com.testcases.Product.D_GetHierarchyLocationID;

import wrappers.API_WrapperClass;
import wrappers.Baseclass;

public class TC_04_GetHierarchyID extends Baseclass{
public TC_04_GetHierarchyID(String baseURL) {
		super(baseURL);
		// TODO Auto-generated constructor stub
	}

D_GetHierarchyLocationID gethierarchy;	

@Test(priority = 3)
public void Get_HierarchyID() throws CsvException {
	gethierarchy = new D_GetHierarchyLocationID();
String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
ExtentReport.createdescription(methodName, "<b><I><h2>Sending request to get Hierarchy ID </h2></I></b>");

gethierarchy.fetchTopHierarchyAndSaveToNewCSV();
    
}

}
