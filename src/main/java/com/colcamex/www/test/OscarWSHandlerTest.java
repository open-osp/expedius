package com.colcamex.www.test;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.oscarehr.ws.Exception_Exception;

import com.colcamex.www.handler.OscarWSHandler;

public class OscarWSHandlerTest {

	private static OscarWSHandler oscarWSHandler;
	
	@BeforeClass
	public static void setUp() throws Exception {
		
		System.setProperty( "javax.net.debug", "all");
		
		// oscarWSHandler = new OscarWSHandler("eupload", "richmond6431");
		oscarWSHandler = new OscarWSHandler("oscardoc", "mac2002");
	}

	// @Test
	public void testLogin() {
		System.out.println("testLogin()");
		System.out.println( "Result: " + oscarWSHandler.login() );
	}

	@Test
	public void testSaveHL7() {
		System.out.println("testSaveHL7()");
		try {
			System.out.println( "RESULT: " + oscarWSHandler.saveHL7("ExpediusExcelleris.20140330184408", "999999") );
		} catch (Exception_Exception e) {
			e.printStackTrace();
		}
	}


}
