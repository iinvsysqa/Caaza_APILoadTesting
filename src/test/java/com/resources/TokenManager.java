//package com.resources;
//
//import java.util.concurrent.atomic.AtomicReference;
//
//public class TokenManager {
//
//	 private static final AtomicReference<String> adminToken = new AtomicReference<>();
//	    private static final AtomicReference<String> operatorToken = new AtomicReference<>();
//	
////	    private static String adminToken;
////	    private static String operatorToken;
//	    
//	    // Private constructor to prevent instantiation
//	    private TokenManager() {}
//	    
//	    public static synchronized String getAdminToken() {
//	    	String Token = adminToken.get();
//	    	System.out.println("[TokenManager] Current admin token: " + adminToken);
//	        if (Token == null) {
//	            throw new IllegalStateException("Admin token not set. Please login first.");
//	        }
//	        return Token;
//	    }
//	    
//	    public static synchronized void setAdminToken(String token) {
//	    	System.out.println("[TokenManager] Setting admin token: " + token);
//	        adminToken.set(token);
//	    }
//	    
//	    public static synchronized String getOperatorToken() {
//	        if (operatorToken == null) {
//	            throw new IllegalStateException("Operator token not set. Please login first.");
//	        }
//	        return operatorToken;
//	    }
//	    
//	    public static synchronized void setOperatorToken(String token) {
//	        operatorToken = token;
//	    }
//	    
//	    public static synchronized void clearTokens() {
//	        adminToken = null;
//	        operatorToken = null;
//	    }
//	}
//
