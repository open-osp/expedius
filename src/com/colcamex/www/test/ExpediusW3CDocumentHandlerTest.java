package com.colcamex.www.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.colcamex.www.handler.ExpediusW3CDocumentHandler;

public class ExpediusW3CDocumentHandlerTest {
	
	private static ExpediusW3CDocumentHandler documentHandler;
	private static String TEST_FILE_IHA = "iha_poi_hl7_test.xml";
	//private static String TEST_FILE_IHA_II = "iha_poi_hl7_II.xml";
	private static String TEST_FILE_EXCELLERIS = "excelleris_qa.xml";
	private static String TEST_NODE = "message";
	private static String testFile;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testFile = TEST_FILE_EXCELLERIS;	
		documentHandler = new ExpediusW3CDocumentHandler();

		documentHandler.parse(testFile);

		Document document = documentHandler.getDocument();
		System.out.println(document);
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}


	public void testReUseClass() {
		System.out.println("@@@Running testReUseClass@@@");
		testFile = TEST_FILE_IHA;
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(testFile);
		try {
			documentHandler.parse(is);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetRoot() {
		System.out.println("@@@Running testGetRoot.@@@");
		
		System.out.println("Root is: " + documentHandler.getRoot());
	}
	
	@Test
	public void testGetMessageCount() {
		System.out.println("@@@Running testGetMessageCount.@@@");
		
		System.out.println("Message count is: " + documentHandler.getMessageCount());
	}
	
	@Test
	public void testGetNodes() {
		System.out.println("@@@Running testGetNode.@@@");
		System.out.println("Requested Node: " + documentHandler.getNodes(TEST_NODE).item(0));
	}
	
	@Test
	public void testGetHL7Version() {
		System.out.println("@@@Running testGetHL7Version@@@");
		System.out.println("HL7 Version: " + documentHandler.getHL7Version());
		
		assertNotNull(documentHandler.getHL7Version());
	}
	
	@Test
	public void testGetHL7Format() {
		System.out.println("@@@Running testGetHL7Format@@@");
		System.out.println("HL7 Format: " + documentHandler.getHL7Format());
		
		assertNotNull(documentHandler.getHL7Format());
	}
	
	@Test
	public void testGetMessageFormat() {
		System.out.println("@@@Running testGetMessageFormat@@@");
		System.out.println("Message Format: " + documentHandler.getMessageFormat());
	}
	
	@Test
	public void testGetMessageIdList() {
		System.out.println("@@@Running testGetMessageIdList@@@");
		
		String[] messageIds = documentHandler.getMessageIdList();
		for( int i = 0; i < messageIds.length; i++ ) {	
			System.out.println(i + " : "+ messageIds[i]);
		}
		
		if( TEST_FILE_IHA.equalsIgnoreCase(testFile) ) {
			assertEquals("LAB4367575.hl7", messageIds[0]);
		} else if( TEST_FILE_EXCELLERIS.equalsIgnoreCase(testFile) ) {
			assertEquals("1", messageIds[0]);
		} else {
			fail();
		}
	}

}
