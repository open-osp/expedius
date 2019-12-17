package com.colcamex.www.util;

import org.junit.Before;
import org.junit.Test;

public class ExpediusPropertiesTest {

	@Before
	public void setUp() throws Exception {	
	
	}

	@Test
	public void testGetKnownPath() {
		System.out.println("@@testGetKnownPath()@@");
		ExpediusProperties properties = ExpediusProperties.getProperties("/var/lib/expedius/expedius.properties");
		System.out.println("" + properties.getProperty("TRUSTSTORE_ALIAS"));
	}
	
	@Test
	public void testUnknownPath() {
		System.out.println("@@testUnknownPath()@@");
		ExpediusProperties properties = ExpediusProperties.getProperties(null);
		System.out.println("" + properties.getProperty("ADMIN_EMAIL"));
	}
	
	@Test
	public void testSingleton() {
		System.out.println("@@testSingleton()@@");
		ExpediusProperties.getProperties("/var/lib/expedius/expedius.properties");
		System.out.println("" + ExpediusProperties.getProperties().getProperty("TRUSTSTORE_ALIAS"));
	}

}
