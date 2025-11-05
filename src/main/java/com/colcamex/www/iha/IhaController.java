package com.colcamex.www.iha;

import java.io.IOException;
import java.net.SocketTimeoutException;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;

import com.colcamex.www.bean.ConfigurationBeanInterface;
import com.colcamex.www.handler.ExpediusMessageHandler;
import com.colcamex.www.handler.ExpediusW3CDocumentHandler;
import com.colcamex.www.http.ExpediusConnect;
import com.colcamex.www.http.AbstractConnectionController;
import com.colcamex.www.util.ExpediusProperties;


public class IhaController extends AbstractConnectionController {
	
	private static final String DEFAULT_IHAPOI_LAB_TYPE = "IHA";
	/*
	 *  Root tag "messages" contains children "message". Each message is an HL7 lab with a unique id.
	 *  Each lab report contains attributes: format="HL7" msgId="LAB4315059.hl7" version="2.3"
	 */
	private String labType;

	public IhaController(ConfigurationBeanInterface configurationBean, ExpediusProperties properties) {
		super(configurationBean, properties);
		
		if(super.properties.containsKey("IHA_POI_LAB_TYPE")) {
			this.labType = super.properties.getProperty("IHA_POI_LAB_TYPE").trim();
		} else {
			this.labType = DEFAULT_IHAPOI_LAB_TYPE;
		}
	}

	@Override
	public void run() {
		Thread thread = Thread.currentThread();
		thread.setName("ihaController"+ "[" + thread.getId() + "]");
		
		logger.debug("Running Thread [" + thread.getName() + "]");

		String fetchLink = null;
		String ackLink = null;
		String[] acknowledgeIds = null;

		if( hostLogin() ) {

			if( getConnection().getResponseCode() == HttpsURLConnection.HTTP_OK ) {
				
				if( FETCH.contains("@username") && FETCH.contains("@password") ) {		
					fetchLink = FETCH.replaceAll("@username", USER).replaceAll("@password", PASS);
				}
				
				try {
					logger.info("Fetching IHA lab files.");
					getConnection().fetch(fetchLink);
				} catch (SocketTimeoutException e) {				
					handleError("Connection timeout occured while attempting to fetch lab files. Check internet connectivity. ", e, ERROR, true);
				} catch (IOException e) {				
					handleError("Expedius has failed to fetch lab files. Contact support. ", e, ERROR, true);
				} catch (ParserConfigurationException e) {				
					handleError("There was a problem with parsing the server response while fetching lab files.", e, ERROR, true);
				} 
			}
			
			if( getConnection().getResponseCode() == HttpsURLConnection.HTTP_OK ) {
				
				if( getConnection().hasResponse() ) {					
					processResults(super.getDocumentHandler(), labType);	
				} else {
					logger.info("Distribution service did not return a response.");
				}				
			} 
			
			if(ACKNOWLEDGE_DOWNLOADS.equalsIgnoreCase("true")) {
				
				// if the lab handler succeeds then all the labs can be acknowledged.
				acknowledgeIds = documentHandler.getMessageIdList();
				int count = 0;
				String id = null;
				int idArraySize = acknowledgeIds.length;
				
				if( getLabHandler().getResponseCode() == HttpsURLConnection.HTTP_OK ) {
					logger.info("Acknowledging " + idArraySize + " IHA lab file(s)");
					ackLink = ACKNOWLEDGE.replaceAll("@username", USER).replaceAll("@password", PASS);
				}
				
				// note the change to getLabHandler response code here.
				while( ( count < 20 ) && ( getLabHandler().getResponseCode() == HttpsURLConnection.HTTP_OK ) ) {
					id = acknowledgeIds[count];

					if( id != null )  {
						ackLink = ackLink.replaceAll("@messageid", id);
					}

					if(ackLink != null) {
						try {
							getConnection().acknowledge(ackLink);
						} catch (SocketTimeoutException e) {
							handleError("Connection timeout occured while attempting to acknowledge lab file. Check internet connectivity. ", e, ERROR, true);
						} catch (IOException e) {
							handleError("Expedius has failed to acknowledge lab files. Contact support. ", e, ERROR, true);
						}
					}
					
					count++;
				}
				
			}	
		}
		
		if( getLabHandler().getResponseCode() == HttpsURLConnection.HTTP_OK ) {
			logger.info("All IHA labs acknowledged successfully.");
		}
		
		super.processServerResponse( getConnection().getResponseCode() );
		
	}

	@Override
	protected boolean hostLogin() {
		
		boolean success = Boolean.TRUE;
		int serverStatus = 0;
		// try link to check connectivity.
		if(getConnection() == null) {
			handleError("Missing connection information. Service not run.", null, ERROR, false);	
		}
		
		try {
			logger.info("Logging in to IHA.");
			getConnection().login(USER, PASS, LOGIN);
		} catch (IOException elogin) {
			success = Boolean.FALSE;
			handleError("Connection error during login. Check login data and connectivity.", elogin, ERROR, false);			
		} finally {			
			serverStatus = getConnection().getResponseCode();

			if(serverStatus > HttpsURLConnection.HTTP_OK) {
				success = Boolean.FALSE;
			}			
		}

		return success;
	}
	

	protected void processResults(ExpediusW3CDocumentHandler documentHandler, String labType) {
			
		// the root structure of IHA labs need to be re-worked to be compatible with Oscar.
		//newDocument = documentHandler.createNewDocument(messages);
		
		// or maybe not?  This holder is here until we find out.

		super.processResults( documentHandler.getDocument(), labType );
	}
	

	@Override
	public boolean start() {
		return start(DOWNLOAD_MODE);
	}

	@Override
	public void setConnection(ExpediusConnect connection) {
		if( (this.connection == null) || (connection == null) ) {
			this.connection = connection;
		}
	}

	@Override
	public void setConfigurationBean(ConfigurationBeanInterface configurationBean) {
		this.configurationBean = configurationBean;		
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
