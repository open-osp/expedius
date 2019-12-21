package com.colcamex.www;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

		Path path = Paths.get(getClass().getClassLoader().getResource("excelleris_qa.xml").getPath());
		
		try {
			String fileContent = new String(Files.readAllBytes(path));
			System.out.println( "RESULT: " + oscarWSHandler.saveHL7(path.getFileName().toString(), fileContent));
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
