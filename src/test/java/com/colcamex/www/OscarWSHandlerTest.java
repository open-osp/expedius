package com.colcamex.www;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.colcamex.www.handler.OscarWSHandler;

public class OscarWSHandlerTest {

	private static OscarWSHandler oscarWSHandler;
	
	@BeforeClass
	public static void setUp() throws Exception {
		oscarWSHandler = new OscarWSHandler("dwarren", "Yessum123");
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		oscarWSHandler = null;
	}


	@Test
	public void testSaveHL7() {
		System.out.println("testSaveHL7()");
		
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("hl7pull_12_15_2019.xml").getFile());
		System.out.println("file path " + file.getAbsolutePath());
		
		try {
			System.out.println( "RESULT: " + oscarWSHandler.saveHL7(file.getAbsolutePath(), "999999") );
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
}
