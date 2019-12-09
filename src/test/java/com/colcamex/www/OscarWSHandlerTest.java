package com.colcamex.www;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.oscarehr.ws.LoginResultTransfer2;

import com.colcamex.www.handler.OscarWSHandler;

public class OscarWSHandlerTest {

	private static OscarWSHandler oscarWSHandler;
	
	@BeforeClass
	public static void setUp() throws Exception {
		oscarWSHandler = new OscarWSHandler("dwarren", "Yessum123");
	}

	// @Test
	public void testLogin() {
		System.out.println("testLogin()");
		LoginResultTransfer2 loginResultTransfer2 = oscarWSHandler.login();
		
		System.out.println( "key: " + loginResultTransfer2.getSecurityTokenKey() );
		System.out.println( "last name : " + loginResultTransfer2.getProvider().getLastName() );
		System.out.println( "security ID : " + loginResultTransfer2.getSecurityId());
	}

	@Test
	public void testSaveHL7() {
		System.out.println("testSaveHL7()");
		
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("excelleris_qa.xml").getFile());
		System.out.println("file path " + file.getAbsolutePath());
		
		try {
			System.out.println( "RESULT: " + oscarWSHandler.saveHL7(file.getAbsolutePath(), "999999") );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}


}
