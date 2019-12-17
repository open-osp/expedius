package com.colcamex.www.http;

import java.io.IOException;
import java.io.InputStream;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
	private static Logger logger = Logger.getLogger("ExpediusConnect"); 

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
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.log(Level.SEVERE, "Expedius connect failed to initialize document factory.");
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
			throws SocketTimeoutException, IOException {

		if( (httpsUri != null) && ( getSocketFactory() != null) ) {
			connect(httpsUri, getSocketFactory());
		} else {
			logger.log(Level.WARNING, "The connection link or SSL socket is missing. Unable to connect.");
		}
	}
	
	public void connect(String httpsUri)
			throws SocketTimeoutException, IOException {
	
		if( (httpsUri != null) && ( getSocketFactory() != null) ) {
			connect(httpsUri, getSocketFactory());
		} else {
			logger.log(Level.WARNING, "The connection link or SSL socket is missing. Unable to connect.");
		}

	}
		
	public void connect(String httpsUri, SSLSocketFactory sf) throws SocketTimeoutException, IOException {
		
		URL path = null;
		if(httpsUri != null) {
			try {
				path = new URL(httpsUri);
			} catch (MalformedURLException e) {
				logger.log(Level.SEVERE, "The URI for connect is not correctly formed.", e);
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
	 * @param excellerisUri
	 * @param protocol
	 * @return
	 * @throws SocketTimeoutException 
	 * @throws HttpException
	 * @throws IOException
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void connect(URL httpsUri, SSLSocketFactory sf) throws SocketTimeoutException, IOException {	
		
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
					logger.log(Level.SEVERE, "Expedius connection manager failed to parse a server response during Connect.", e);
				} finally {			
					close();		
					logger.info("Expedius connection status is HttpsURLConnection [" + responseCode + "]");
				}
			}
		}
	}
	
	/**
	 * Overloaded for String input with place holders @password and @username.
	 * @param username
	 * @param password
	 * @param httpsUri
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public void login(String username, String password, String httpsUri) 
			throws SocketTimeoutException, IOException {

		if((username != null) && (password != null)) {
			
			if( httpsUri.contains("@username") && httpsUri.contains("@password") ) {		
				httpsUri = httpsUri.replaceAll("@username", username).replaceAll("@password", password);
			}
			
			login(httpsUri);
		}		

	}
	
	public void login(String query) throws SocketTimeoutException, IOException {
		
		URL path = null;
		try {	
			path = new URL(query);
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, "The URI for login is not correctly formed.", e);
			setResponseCode(HttpsURLConnection.HTTP_INTERNAL_ERROR);
		} finally {
			if(path != null) {
				login(path);
			}
		}
	}

	/**
	 * Login method parses the login script with a login username and password.
	 * 
	 * @param username
	 * @param password
	 * @param query
	 * @return
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws SocketTimeoutException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws Exception
	 */
	public void login(URL query) throws SocketTimeoutException, IOException {
		
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
					logger.log(Level.WARNING, "Expedius connection manager failed to parse a server response during Login.", e);
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
	 * @param httpsUri
	 * @return
	 * @throws MalformedURLException 
	 * @throws SocketTimeoutException 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public void fetch(String httpsUri) throws SocketTimeoutException, IOException, ParserConfigurationException  {
		URL path = null;
		try {
			path = new URL(httpsUri);
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, "The URI for fetch is not correctly formed.", e);
			setResponseCode(HttpsURLConnection.HTTP_INTERNAL_ERROR);
		} finally {
			if(path != null) {
				fetch(path);
			}
		}
	}
		
	/**
	 * Fetch the xml file. (HL7)
	 * @param query
	 * @return HTTP response code
	 * @throws SocketTimeoutException 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public void fetch(URL httpsUri) throws SocketTimeoutException, IOException, ParserConfigurationException {	

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
				logger.log(Level.WARNING, "Expedius connection manager failed to parse a server response during Fetch.", e);
			} finally {
				close();
			}
		} else {
			setResponseCode(HttpsURLConnection.HTTP_NO_CONTENT);
		}
		
	}
	
	/**
	 * Option to acknowledge batches of labs. As for the IHA POI.
	 * @param httpsUriList
	 * @return
	 * @throws SocketTimeoutException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void acknowledge(ArrayList<String> httpsUriList) 
			throws SocketTimeoutException, IOException {
	
		if(httpsUriList.size() > 0) {
			Iterator<String> httpsList = httpsUriList.iterator();
			while( (getResponseCode() == HttpsURLConnection.HTTP_OK) && (httpsList.hasNext()) ){
				acknowledge(httpsList.next());
			}
		} 
			
		setResponseCode(sconn.getResponseCode());

	}
	
	public void acknowledge(String httpsUri) 
			throws SocketTimeoutException, IOException {
		URL path = null;
		try{
			path = new URL(httpsUri);
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, "The URI for acknowledge is not correctly formed.", e);
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
	 * 
	 * @return HTTP response code.
	 * @param query
	 * @throws IOException
	 * @throws SAXException 
	 */
	public void acknowledge(URL httpsUri) throws SocketTimeoutException, IOException {

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
				logger.log(Level.WARNING, "Expedius connection manager failed to parse a server response during Acknowledge.", e);
			} finally {
				close();
			}
		}
	}
	
	/**
	 * 
	 * @param httpsUri
	 * @return
	 * @throws SocketTimeoutException
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 */
	public void logout(String httpsUri) 
			throws SocketTimeoutException, IOException {
		URL path = null;
		try {	
			path = new URL(httpsUri);
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, "The URI for logout is not correctly formed.", e);
			setResponseCode(HttpsURLConnection.HTTP_INTERNAL_ERROR);
		} finally {
			if(path != null) {
				logout(path);
			}
		}  
	}
	
	/**
	 * Logout and close the connection.
	 * Is is important to logout to maintain security.
	 * 
	 * @param query
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException 
	 */
	public void logout(URL httpsUri) throws SocketTimeoutException, IOException {

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
				logger.log(Level.WARNING, "Expedius connection manager failed to parse a server response.", e);
			} finally {
				close();				
				setLoggedIn(Boolean.FALSE);				
				logger.info("Disconnected");
			}

		}
	}
	
	
	/**
	 * Close all statement. 
	 * @throws IOException 
	 */
	public void close() throws SocketTimeoutException, IOException {
		
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
	 * @return
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
	 * @param query
	 * @return
	 * @throws IOException
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
