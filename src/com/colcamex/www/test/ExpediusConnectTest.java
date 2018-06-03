package com.colcamex.www.test;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.net.ssl.SSLSocketFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.colcamex.www.handler.ExpediusW3CDocumentHandler;
import com.colcamex.www.http.ExpediusConnect;
import com.colcamex.www.security.SSLSocket;
import com.colcamex.www.util.ExpediusProperties;

public class ExpediusConnectTest {
	
	private static ExpediusConnect connection;
	private static String  URI;
	private static String  LOGIN;
	private static String  REQUEST_NEW;
	//private static String  ACK_POSITIVE;
	private static String  LOGOUT;
	private static String  USER;
	private static String  PASS;
	private static String  TRUSTSTORE_URL; 
	private static String  KEYSTORE_URL;
	private static String  STORE_PASS;
	private static String  STORE_TYPE;;
	private static String  HTTPS_PROTOCOL;
	private static ExpediusProperties properties;
	private static SSLSocketFactory socketFactory; 
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("SETUP");
		
		properties = ExpediusProperties.getProperties("/var/lib/expedius/expedius.properties");
		
		// using Excelleris for the test.
		if(properties != null) {
			
			TRUSTSTORE_URL = properties.getProperty("TRUSTSTORE_URL").trim();
			STORE_TYPE = properties.getProperty("STORE_TYPE").trim();
			STORE_PASS = properties.getProperty("STORE_PASS").trim();
			HTTPS_PROTOCOL = properties.getProperty("HTTPS_PROTOCOL").trim();
			KEYSTORE_URL = properties.getProperty("KEYSTORE_URL").trim();
			//ACK_POSITIVE = properties.getProperty("ACKNOWLEDGE_DOWNLOADS").trim();
			USER = "oscartreatment1";
			//USER = ".p-poi-t-Colcamex";
			PASS = "oscar1";
			//PASS = "test600test";
			
			//USER = "vendor1";
			//PASS =  "vendor";
			
//			URI = properties.getProperty("IHA_URI").trim();
//			LOGIN = "https://emr.ehealth.interiorhealth.ca/poitest/interface.aspx?usr=.p-poi-t-Colcamex&pwd=test600test";	
//			REQUEST_NEW = "https://emr.ehealth.interiorhealth.ca/poitest/interface.aspx?usr=.p-poi-t-Colcamex&pwd=test600test&cmd=GETMESSAGE";
//			ACK_POSITIVE = "https://emr.ehealth.interiorhealth.ca/poitest/interface.aspx?usr=.p-poi-t-Colcamex&pwd=test600test&cmd=ACKNOWLEDGE&msgid=@messageid";
//			LOGOUT = "https://emr.ehealth.interiorhealth.ca/poitest/interface.aspx?usr=.p-poi-t-Colcamex&pwd=test600test&cmd=LOGOUT";
			
			
			URI = properties.getProperty("EXCELLERIS_URI").trim();
			LOGIN = properties.getProperty("LOGIN").trim();	
			REQUEST_NEW = properties.getProperty("REQUEST_NEW").trim();
			// ACK_POSITIVE = properties.getProperty("ACK_POSITIVE").trim();
			LOGOUT = properties.getProperty("LOGOUT").trim();
			
			System.out.println("TRUSTSTORE_URL: "+TRUSTSTORE_URL);
			System.out.println("STORE_TYPE: "+STORE_TYPE);
			System.out.println("STORE_PASS: "+STORE_PASS);
			System.out.println("HTTPS_PROTOCOL: "+HTTPS_PROTOCOL);
			System.out.println("KEYSTORE_URL: "+KEYSTORE_URL);
			
			SSLSocket sslSocket = SSLSocket.getInstance(TRUSTSTORE_URL, STORE_TYPE, STORE_PASS, HTTPS_PROTOCOL, KEYSTORE_URL);
			socketFactory = sslSocket.getSocketFactory();

			ExpediusConnectTest.connection = ExpediusConnect.getInstance(new ExpediusW3CDocumentHandler());	
			
//			System.out.println("Protocol: " + sslContext.getProtocol());
//			System.out.println("Provider: " + sslContext.getProvider());
//			
//			for(int i = 0; i < sslContext.getSupportedSSLParameters().getProtocols().length; i++ ) {
//				System.out.println("Supported Protocol: " + i + " " + sslContext.getSupportedSSLParameters().getProtocols()[i]);
//				
//				System.out.println("Cipher suite : " + i + " " + sslContext.getSupportedSSLParameters().getCipherSuites()[i]);
//				
//				System.out.println("Default Parameters : " + i + " " + sslContext.getDefaultSSLParameters().getProtocols()[i]);
//			}
			
			
		}
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("TEARDOWN");
		connection.close();
	}
	
	@Test
	public void testFetch() {
		testConnect();
		testLogin();
		testFetchString();
		testLogout();
	}

	
	public void testLogin() {		
		System.out.println("Running testLogin");

		Document response = null;
		try {
			connection.login(USER, PASS, LOGIN);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		if(connection.hasResponse()) {
			response = connection.getResponse();
		}
		
		Node rootTag = response.getDocumentElement();
		
		System.out.println("Login status: " + connection.getResponseCode());
		System.out.println("Login response: " + rootTag.getFirstChild().getNodeValue());

	}

	public void testFetchString() {
				
		System.out.println("Running testFetchString");

		Document response = null;
		Document result = null;
		
		try {
			connection.fetch(REQUEST_NEW);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(connection.hasResponse()) {
			response = connection.getResponse();
			Node root = response.getDocumentElement();
			System.out.println("Fetch response: " + root.getNodeName());

			NamedNodeMap nodeList = root.getAttributes();
		    for (int i = 0; i < nodeList.getLength(); i++) {
		    	Attr attr = (Attr) nodeList.item(i);
	            System.out.println(attr);

		    }
		}
		
		if(connection.hasResponse()) {
			result = ((ExpediusW3CDocumentHandler) connection.getDocumentHandler()).getDocument(); 
			
			// or
			
			//result = connection.getResponse();
			
			Node root = result.getDocumentElement();
			System.out.println("Fetch Result: "+root.getNodeName());
			NodeList nodeList = root.getChildNodes();
		    for (int i = 0; i < nodeList.getLength(); i++) {
		        Node node = nodeList.item(i);
		        //if (node.getNodeType() == Node.ELEMENT_NODE) {
		            // do something with the current element
		        	//Node childNode = 
		            System.out.println(node.getNodeName());
		        //}
		    }
		    System.out.println("############################ End Fetch Result ############################");
			
		}
		
		System.out.println("Fetch status: " + connection.getResponseCode());
	
	}

	
	public void testLogout() {
		System.out.println("Running testLogout");

		Document response = null;
		try {
			connection.logout(LOGOUT);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		if(connection.hasResponse()) {
			response = connection.getResponse();
			System.out.println("Logout response: " + response.getDocumentElement().getNodeName());
		}
		
		System.out.println(connection.getResponseCode());
		
	
	}

	
	public void testConnect() {
		System.out.println("Running testConnect");

		try {
			connection.connect(URI, socketFactory);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Connect status: "+connection.getResponseCode());
		
		if(connection.hasResponse()) {
			System.out.println("Connect response: " + connection.getResponse().getDocumentElement().getNodeName());
			
			
		}

	}
	

	
}
