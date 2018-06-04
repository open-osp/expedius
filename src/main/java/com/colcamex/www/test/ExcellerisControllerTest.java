package com.colcamex.www.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.colcamex.www.bean.ConfigurationBeanInterface;
import com.colcamex.www.excelleris.ExcellerisController;
import com.colcamex.www.handler.ExpediusHL7LabHandler;
import com.colcamex.www.handler.ExpediusMessageHandler;
import com.colcamex.www.handler.ExpediusW3CDocumentHandler;
import com.colcamex.www.http.ExpediusConnect;
import com.colcamex.www.http.AbstractConnectionController;
import com.colcamex.www.util.BeanRetrieval;
import com.colcamex.www.util.ExpediusProperties;

public class ExcellerisControllerTest {

	private static AbstractConnectionController excellerisController;
	private static ExpediusProperties properties;
	private static ConfigurationBeanInterface configBean;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("SETUP");
		
		properties = ExpediusProperties.getProperties("/var/lib/expedius/expedius.properties");
		
		// using Excelleris for the test.
		if(properties != null) {
			ExpediusW3CDocumentHandler documentHandler = new ExpediusW3CDocumentHandler();
			ExpediusConnect connection = ExpediusConnect.getInstance(documentHandler);
			configBean = (ConfigurationBeanInterface) BeanRetrieval.getBean("ExcellerisConfigurationBean");
			excellerisController = new ExcellerisController(properties, configBean);
			excellerisController.setLabHandler(new ExpediusHL7LabHandler(properties));
			excellerisController.setDocumentHandler(documentHandler);
			excellerisController.setConnection(connection);
			excellerisController.setMessageHandler(new ExpediusMessageHandler());
			excellerisController.setServiceName("IHAPOI");
		}		
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

	@Test
	public void test() {
		System.out.println("Test run method.");
		
		excellerisController.run();
		
		fail("Not yet implemented");
	}

}
