package com.colcamex.www;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.colcamex.www.bean.ConfigurationBeanInterface;
import com.colcamex.www.excelleris.ExcellerisConfigurationBean;
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

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("SETUP");
		
		ExpediusProperties.getProperties();
		
		ExcellerisConfigurationBean configurationBean = new ExcellerisConfigurationBean();
		configurationBean.initialize(
				ExpediusProperties.getProperties().getProperty("EXCELLERIS_URI"),	
				ExpediusProperties.getProperties().getProperty("REQUEST_NEW"),
				ExpediusProperties.getProperties().getProperty("LOGIN"),
				ExpediusProperties.getProperties().getProperty("LOGOUT"),
				ExpediusProperties.getProperties().getProperty("ACK_POSITIVE")
		);	
		configurationBean.setServiceName("excelleris");
		BeanRetrieval.setBean(configurationBean);
		
		
		// using Excelleris for the test.
		ExpediusW3CDocumentHandler documentHandler = new ExpediusW3CDocumentHandler();
		ExpediusConnect connection = ExpediusConnect.getInstance(documentHandler);

		excellerisController = new ExcellerisController(BeanRetrieval.getBean(ExcellerisConfigurationBean.class), ExpediusProperties.getProperties());
		excellerisController.setLabHandler(new ExpediusHL7LabHandler(ExpediusProperties.getProperties()));
		excellerisController.setDocumentHandler(documentHandler);
		excellerisController.setConnection(connection);
		excellerisController.setMessageHandler(new ExpediusMessageHandler());
		excellerisController.setServiceName("EXCELLERIS");	
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

	}

}
