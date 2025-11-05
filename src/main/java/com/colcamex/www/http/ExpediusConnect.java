package com.colcamex.www.http;

import java.io.IOException;
import java.io.InputStream;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.colcamex.www.handler.ExpediusW3CDocumentHandler;


/**
 * This is a singleton class of Expedius Connect.
 * Handles all the connection points for a variety of 
 * electronic lab services.
 * 
 * @author dennis warren @ Colcamex Resources 
 */
public class ExpediusConnect {
	
	private static ExpediusConnect instance = null;

	// all connection logging is handled by java.util.logging in a custom log formatter.
	private static final Logger logger = LogManager.getLogger("ExpediusConnect");

	private HttpsURLConnection sconn;	
	private SSLSocketFactory socketFactory;
	private ExpediusW3CDocumentHandler documentHandler;
	private DocumentBuilder documentBuilder;
	private boolean hasResponse;
	private Document response;
	private InputStream in;
	private boolean loggedIn;
	private int responseCode;
	
	private ExpediusConnect() { 
		DocumentBuilderFactory documentBuilderFactory =  DocumentBuilderFactory.newInstance();
		
		// Secure against XXE attacks
		try {
			documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
			documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			documentBuilderFactory.setXIncludeAware(false);
			documentBuilderFactory.setExpandEntityReferences(false);
		} catch (ParserConfigurationException e) {
			logger.error("Failed to configure secure XML parsing features.", e);
		}
		
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error("Expedius connect failed to initialize document factory.", e);
		}
		_init();
	}

	private ExpediusConnect(ExpediusW3CDocumentHandler documentBuilder) { 
		setDocumentHandler( documentBuilder );
		_init();
	}
	
	private void _init() {
		CookieManager cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cookieManager);
	}
	
	/**
	 * Static constructor. Returns an instance of Connect.
	 * Set with Expedius Document Handler or null for default generic dom parser.
	 * 
	 * @return ExpediusConnect connection class.
	 */
	public static ExpediusConnect getInstance(ExpediusW3CDocumentHandler expediusDocumentBuilder) { 

		if(instance == null) {
			
			if (expediusDocumentBuilder == null) {
				instance = new ExpediusConnect();
			} else {
				instance = new ExpediusConnect(expediusDocumentBuilder);
			}
			
		} 
						
		return instance;
	}
	
	public void connect(URL httpsUri)
			throws IOException {

		if( (httpsUri != null) && ( getSocketFactory() != null) ) {
			connect(httpsUri, getSocketFactory());
		} else {
			logger.warn("The connection link or SSL socket is missing. Unable to connect.");
		}
	}
	
	public void connect(String httpsUri)
			throws IOException {
	
		if( (httpsUri != null) && ( getSocketFactory() != null) ) {
			connect(httpsUri, getSocketFactory());
		} else {
			logger.warn( "The connection link or SSL socket is missing. Unable to connect.");
		}

	}
		
	public void connect(String httpsUri, SSLSocketFactory sf) throws IOException {
		
		URL path = null;
		if(httpsUri != null) {
			try {
				path = new URL(httpsUri);
			} catch (MalformedURLException e) {
				logger.error( "The URI for connect is not correctly formed.", e);
				setResponseCode(HttpsURLConnection.HTTP_INTERNAL_ERROR);
			} finally {
				if(path != null) {
					connect(path, sf);
				}
			}	
		}

	}
	
	/**
	 * Connection method. Confirms connection to server and sets up
	 * any security certificates required.
	 *
	 */
	public void connect(URL httpsUri, SSLSocketFactory sf) throws IOException {
		
		setHasResponse(Boolean.FALSE);
		setSocketFactory(sf);	

		in = execute(httpsUri);	
		
		if(in != null) {
			
			if(in.available() > 0) {	

				try {
					if(documentHandler != null) {
						setResponse( documentHandler.parse(in) );
					} else if (documentBuilder != null) {
						setResponse( documentBuilder.parse(in) );
					}					
					setHasResponse(Boolean.TRUE);
				} catch (SAXException e) {
					logger.error( "Expedius connection manager failed to parse a server response during Connect.", e);
				} finally {			
					close();		
					logger.info("Expedius connection status is HttpsURLConnection [" + responseCode + "]");
				}
			}
		}
	}
	
	/**
	 * Overloaded for String input with place holders @password and @username.
	 */
	public void login(String username, String password, String httpsUri) 
			throws IOException {

		if((username != null) && (password != null)) {
			
			if( httpsUri.contains("@username") && httpsUri.contains("@password") ) {		
				httpsUri = httpsUri.replaceAll("@username", username).replaceAll("@password", password);
			}
			
			login(httpsUri);
		}		

	}
	
	public void login(String query) throws IOException {
		
		URL path = null;
		try {	
			path = new URL(query);
		} catch (MalformedURLException e) {
			logger.error( "The URI for login is not correctly formed.", e);
			setResponseCode(HttpsURLConnection.HTTP_INTERNAL_ERROR);
		} finally {
			if(path != null) {
				login(path);
			}
		}
	}

	/**
	 * Login method parses the login script with a login username and password.
	 */
	public void login(URL query) throws IOException {
		
		setHasResponse(Boolean.FALSE);
		
		if(query != null){

			in = execute(query);
			
			if(in != null && in.available() > 0) {
			
				try {
					if(documentHandler != null) {
						setResponse( documentHandler.parse(in) );
					} else if (documentBuilder != null) {
						setResponse( documentBuilder.parse(in) );
					}
					setHasResponse(Boolean.TRUE);
				} catch (SAXException e) {
					logger.error( "Expedius connection manager failed to parse a server response during Login.", e);
					setResponseCode(HttpsURLConnection.HTTP_INTERNAL_ERROR);
				} finally {
					if(getResponseCode() == HttpsURLConnection.HTTP_OK) {
						setLoggedIn(Boolean.TRUE);
					}
		
					close();						
				}
				
			}			
		}
	}
	
	/**
	 * Overload for string input.
	 */
	public void fetch(String httpsUri) throws IOException, ParserConfigurationException  {
		URL path = null;
		try {
			path = new URL(httpsUri);
		} catch (MalformedURLException e) {
			logger.error( "The URI for fetch is not correctly formed.", e);
			setResponseCode(HttpsURLConnection.HTTP_INTERNAL_ERROR);
		} finally {
			if(path != null) {
				fetch(path);
			}
		}
	}
		
	/**
	 * Fetch the xml file. (HL7)
	 */
	public void fetch(URL httpsUri) throws IOException, ParserConfigurationException {

		setHasResponse(Boolean.FALSE);
		in = execute(httpsUri);	

		if(in != null && in.available() > 0) {

			try {							
				if(documentHandler != null) {	
					documentHandler.parse(in);
				} else if (documentBuilder != null) {
					documentBuilder.parse(in);
				}
				setHasResponse(Boolean.TRUE);
			} catch (SAXException e) {
				logger.error( "Expedius connection manager failed to parse a server response during Fetch.", e);
			} finally {
				close();
			}
		} else {
			setResponseCode(HttpsURLConnection.HTTP_NO_CONTENT);
		}
		
	}
	
	/**
	 * Option to acknowledge batches of labs. As for the IHA POI.
	 */
	public void acknowledge(ArrayList<String> httpsUriList) 
			throws IOException {
	
		if(httpsUriList.size() > 0) {
			Iterator<String> httpsList = httpsUriList.iterator();
			while( (getResponseCode() == HttpsURLConnection.HTTP_OK) && (httpsList.hasNext()) ){
				acknowledge(httpsList.next());
			}
		} 
			
		setResponseCode(sconn.getResponseCode());

	}
	
	public void acknowledge(String httpsUri) 
			throws IOException {
		URL path = null;
		try{
			path = new URL(httpsUri);
		} catch (MalformedURLException e) {
			logger.error( "The URI for acknowledge is not correctly formed.", e);
			setResponseCode(HttpsURLConnection.HTTP_INTERNAL_ERROR);
		} finally {
			if(path != null) {
				acknowledge(path);
			}
		} 
	}
	
	/**
	 * Send an acknowledge signal that the labs were 
	 * retrieved.  This way the same labs will not get down loaded again
	 */
	public void acknowledge(URL httpsUri) throws IOException {

		setHasResponse(Boolean.FALSE);
		in = execute(httpsUri);
		
		if(in != null && in.available() > 0) {
			try {
				if(documentHandler != null) {
					setResponse( documentHandler.parse(in) );
				} else if (documentBuilder != null) {
					setResponse( documentBuilder.parse(in) );
				}
				setHasResponse(Boolean.TRUE);
			} catch (SAXException e) {
				logger.error( "Expedius connection manager failed to parse a server response during Acknowledge.", e);
			} finally {
				close();
			}
		}
	}

	public void logout(String httpsUri) 
			throws IOException {
		URL path = null;
		try {	
			path = new URL(httpsUri);
		} catch (MalformedURLException e) {
			logger.error( "The URI for logout is not correctly formed.", e);
			setResponseCode(HttpsURLConnection.HTTP_INTERNAL_ERROR);
		} finally {
			if(path != null) {
				logout(path);
			}
		}  
	}
	
	/**
	 * Logout and close the connection.
	 * It is important to logout to maintain security.
	 */
	public void logout(URL httpsUri) throws IOException {

		setHasResponse(Boolean.FALSE);
		
		if(isLoggedIn()) {
			in = execute(httpsUri);	
		}
		
		if(in != null && in.available() > 0) {

			// this needs to be worked around because Excelleris 
			// returns an invalid response.				
			try {
				if(documentHandler != null) {
					setResponse( documentHandler.parse(in) );
				} else if (documentBuilder != null) {
					setResponse( documentBuilder.parse(in) );
				}
				setHasResponse(Boolean.TRUE);
			} catch (SAXException e) {
				logger.error( "Expedius connection manager failed to parse a server response.", e);
			} finally {
				close();				
				setLoggedIn(Boolean.FALSE);				
				logger.info("Disconnected");
			}

		}
	}
	
	
	/**
	 * Close all statement.
	 */
	public void close() throws IOException {
		
		if(in != null) {
			in.close();
		}
		
		if(sconn != null) {
			sconn.disconnect();
		}		
	}

	public SSLSocketFactory getSocketFactory() {
		return socketFactory;
	}

	public void setSocketFactory(SSLSocketFactory socketFactory) {
		this.socketFactory = socketFactory;
	}

	/**
	 * A Switch for when there are lab results to retrieve.
	 * @return Boolean
	 */
	public boolean hasResponse() {
		return hasResponse;
	}
	
	private void setHasResponse(boolean response) {
		this.hasResponse = response;
	}
	
	private void setResponse(Document response) {
		this.response = response;
	}

	public Document getResponse() {
		return this.response;
	}

	public int getResponseCode() {
		return responseCode;
	}

	private void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * Current logged in status 
	 * @return boolean
	 */
	public boolean isLoggedIn() {
		return loggedIn;
	}


	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	/**
	 * Execute get and post commands to the server
	 * Returns an input stream.
	 */
	private InputStream execute(URL httpsUri) throws IOException {

		if(httpsUri != null) {

			sconn = (HttpsURLConnection) httpsUri.openConnection();
			sconn.setConnectTimeout(60000);
			sconn.setReadTimeout(300000);
			sconn.setRequestMethod("GET");
			//sconn.setDoOutput(false);
			sconn.setDoInput(true);
			
			if(getSocketFactory() != null) {
				sconn.setSSLSocketFactory(this.getSocketFactory());
			}
			
			sconn.connect();

			setResponseCode(sconn.getResponseCode());
			
			if(getResponseCode() == HttpsURLConnection.HTTP_OK) {
				return sconn.getInputStream();
			}				
		}
			
		return null;		
	}

	public ExpediusW3CDocumentHandler getDocumentHandler() {
		return documentHandler;
	}

	public void setDocumentHandler(ExpediusW3CDocumentHandler documentHandler) {
		this.documentHandler = documentHandler;
	}

}
