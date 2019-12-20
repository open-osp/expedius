
package com.colcamex.www.excelleris; 

import java.io.IOException; 
import java.net.SocketTimeoutException;

import javax.net.ssl.*;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.colcamex.www.bean.ConfigurationBeanInterface;
import com.colcamex.www.handler.ExpediusMessageHandler;
import com.colcamex.www.handler.ExpediusW3CDocumentHandler;
import com.colcamex.www.http.ExpediusConnect;
import com.colcamex.www.http.AbstractConnectionController;


/**
 * @author Dennis Warren 
 * @ Colcamex Resources
 * dwarren@colcamex.com
 * www.colcamex.com 
 * Date: May 2012.
 * 
 */
public class ExcellerisController extends AbstractConnectionController {

	private static final String DEFAULT_EXCELLERIS_LAB_TYPE = "EXCELLERIS";
	private static final String ACK_RETURN_CODE = "0";
	private static final String RESPONSE_ACCESSGRANTED = "accessgranted";
	private static final String NODE_HL7MESSAGES = "hl7messages";
	private static final String NODE_MESSAGECOUNT = "MessageCount";
	private static final String NODE_MESSAGEFORMAT = "MessageFormat";
	private static final String NODE_VERSION = "Version";
	private static final String NODE_RETURNCODE = "ReturnCode";

	private String labType;

	public ExcellerisController(ConfigurationBeanInterface configurationBean) {		
		super(configurationBean);
		
		if(super.properties.containsKey("EXCELLERIS_LAB_TYPE")) {
			this.labType = properties.getProperty("EXCELLERIS_LAB_TYPE").trim();
		} else {
			this.labType = DEFAULT_EXCELLERIS_LAB_TYPE;
		}
    }
	
	@Override
	public void run() {

		Thread thread = Thread.currentThread();
		thread.setName("ExcellerisController"+ "[" + thread.getId() + "]");
		
		logger.debug("Running [" + thread.getName() + "]");	
		
		Node rootTag = null;
		String messageCount = null;
		String messageFormat = null;
		String messageVersion = null;
		String ackReturnCode = null;
		setLastFileCount(0);
		
		if(hostLogin()){
			
			try {	
				// fetch data
				logger.info("Fetching Excelleris labs from " + FETCH);
				getConnection().fetch(FETCH);
			} catch (SocketTimeoutException e) {				
				handleError("Connection timeout occured while attempting to fetch lab files. Check internet connectivity. ", e, ERROR, true);
			} catch (IOException e) {				
				handleError("Expedius has failed to fetch lab files. Contact support. ", e, ERROR, true);
			} catch (ParserConfigurationException e) {				
				handleError("There was a problem with parsing the server response while fetching lab files.", e, ERROR, true);
			}  

			// save data
			if( getConnection().getResponseCode() == HttpsURLConnection.HTTP_OK  && getConnection().hasResponse()) {

				rootTag = getDocumentHandler().getRoot();
				
				logger.info("HL7 root tag " + rootTag);
				
				if( NODE_HL7MESSAGES.equalsIgnoreCase(rootTag.getNodeName()) ) {
					messageCount = getDocumentHandler().getNodeAttributeValue(NODE_MESSAGECOUNT, rootTag);
					messageFormat = getDocumentHandler().getNodeAttributeValue(NODE_MESSAGEFORMAT, rootTag);
					messageVersion = getDocumentHandler().getNodeAttributeValue(NODE_VERSION, rootTag);
					
					logger.info(NODE_MESSAGECOUNT + ": " + messageCount);
					logger.info(NODE_MESSAGEFORMAT + ": " + messageFormat);
					logger.info(NODE_VERSION + ": " + messageVersion);
				}

				/*
				 * First processResults: save the W3C Document to the filesystem 
				 * If success then parseAndPersist: fetch the Oscar EMR endpoint to push the 
				 * lab results to Oscar.
				 */
				if(processResults(getDocumentHandler().getDocument(), labType)) {
					parseAndPersist();
				}
				
			}

			// CAUTION - disable acknowledge for testing. You will loose all your test labs.
			if( (ACKNOWLEDGE_DOWNLOADS.equalsIgnoreCase("true")) && (getLabHandler().getResponseCode() == HttpsURLConnection.HTTP_OK) ) {
				
				/*
				 *  if the lab handler succeeds then all the labs can be acknowledged.
				 *  acknowledgeIds = documentHandler.getMessageIdList();
				 */
				try {
					getConnection().acknowledge(ACKNOWLEDGE);
				} catch (SocketTimeoutException e) {				
					handleError("Connection timeout occured while attempting to fetch lab files. Check internet connectivity. ", e, ERROR, true);
				} catch (IOException e) {				
					handleError("Expedius has failed to fetch lab files. Contact support. ", e, ERROR, true);
				} 
				
				if(getConnection().hasResponse()) {	
					
					ackReturnCode = getDocumentHandler().getNodeAttributeValue(NODE_RETURNCODE, getDocumentHandler().getRoot());
					
					if( ACK_RETURN_CODE.equalsIgnoreCase(ackReturnCode) ) {
						logger.info("All Labs Acknowledged: " + ackReturnCode);
					} else {
						handleError("Expedius has failed to acknowledge the last lab download. "
								+ "This could result in multiple copies of the same lab. Excelleris server acknowledge return code was "
								+ ackReturnCode + "If this error does not resolve in 24 hours, contact support.",
								null, DISMISSABLE_ERROR, true);
					}
				}
			} else {
				logger.info("Acknowledge not enabled. This lab file will be downloaded again.");
			}
							
			// catch all server response codes.
			processServerResponse(getConnection().getResponseCode());
			
			close();
		}
	}	
	
	/**
	 * Login method. Used on every connection and whenever the server needs to be verified.
	 */
	@Override
	protected boolean hostLogin() {

		boolean success = Boolean.FALSE;
		String result = null;	
		SSLSocketFactory socketFactory = super.getSSLSocketFactory();
		
		if(socketFactory != null) {
			
			if(getConnection() == null) {
				handleError("Missing connection information. Service not run.", null, ERROR, false);	
			}

			// connect - sets up the security keys and session cookie. A handshake.
			try {
				getConnection().connect(URI, socketFactory);
			} catch (SocketTimeoutException e1) {
				handleError("Connection timeout occured during handshake. Check internet connectivity and try again. ", e1, ERROR, false);
			} catch (IOException e2) { 										
				handleError("Server is not recognized or cannot be reached during handshake. Check connection links or if server is accessable. ", e2, ERROR, false);			
			}
						
			/*
			 *  authenticate - verify login information and open portal.
			 *  but only after a handshake success.
			 */
			if(getConnection().getResponseCode() == HttpsURLConnection.HTTP_OK) {
				
				try {
					getConnection().login(USER, PASS, LOGIN);
				} catch (SocketTimeoutException e3) {					
					handleError("Connection timeout occured while logging-in. Check internet connectivity and try again. ", e3, ERROR, false);
				} catch (IOException e4) {					
					handleError("Connection failure while logging-in. Check internet connectivity and try again. ", e4, ERROR, false);
				}
				
			}

			if(getConnection().getResponseCode() == HttpsURLConnection.HTTP_OK) {
				
				if(getConnection().hasResponse()) {
					
					result = getDocumentHandler().getRoot().getFirstChild().getNodeValue();

					logger.info("Excelleris login response: " + result);
									
					if(result.equalsIgnoreCase(RESPONSE_ACCESSGRANTED)) {
						success = Boolean.TRUE;
					} else {
						logger.info("Failed to log into Excelleris.");
					}					
				}								
			}
			
			if(getConnection().getResponseCode() > HttpsURLConnection.HTTP_OK) {
				processServerResponse(getConnection().getResponseCode());
			}
	
		} 
		
		return success;
		
	}

	public boolean start() {		
		return super.start(DOWNLOAD_MODE);
	}
	
	@Override
	protected boolean processResults(Document results, String labType) {
		return super.processResults(results, labType);
	}
	
	@Override
	public void setConnection(ExpediusConnect connection) {
		this.connection = connection;		
	}

	@Override
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	@Override
	public void setMessageHandler(ExpediusMessageHandler messageHandler) {
		this.messageHandler = messageHandler;		
	}

	@Override
	protected void setLastFileCount(int lastFileCount) {
		this.lastFileCount = lastFileCount;
		
	}

	@Override
	public void setDocumentHandler(ExpediusW3CDocumentHandler documentHandler) {
		this.documentHandler = documentHandler;			
	}
		   
}
