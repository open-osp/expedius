package com.colcamex.www.http;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.lang.Exception;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import com.colcamex.www.bean.ConfigurationBeanInterface;
import com.colcamex.www.handler.ExpediusHL7LabHandler;
import com.colcamex.www.handler.ExpediusMessageHandler;
import com.colcamex.www.handler.ExpediusW3CDocumentHandler;
import com.colcamex.www.security.SSLSocket;
import com.colcamex.www.util.Email;
import com.colcamex.www.util.ExpediusProperties;

/**
 * @author Dennis Warren 
 * @ Colcamex Resources
 * dwarren@colcamex.com
 * www.colcamex.com 
 * Date: May 2012.
 * 
 */
public abstract class AbstractConnectionController implements Runnable {

	protected static Logger logger = LogManager.getLogger(AbstractConnectionController.class);
//	protected static Logger customLogger = null;

	public static final int DOWNLOAD_MODE = 1;
	public static final int LOGIN_MODE = 2;
	protected static final int ERROR = 1;
	protected static final int DISMISSABLE_ERROR = 2;
	
	protected static String  TRUSTSTORE_URL ;
	protected static String  STORE_TYPE ;
	protected static String  STORE_PASS ;
	protected static String  HTTPS_PROTOCOL ;
	protected static String  KEYSTORE_URL ;
	protected static String  USER;
	protected static String  PASS;	
	protected static String  URI;
	protected static String  LOGIN;	
	protected static String  FETCH;
	protected static String  ACKNOWLEDGE;
	protected static String  LOGOUT;
	protected static String  ACKNOWLEDGE_DOWNLOADS;	
	
	protected boolean controllerStatus;
    protected ExpediusConnect connection;
    protected SSLSocket socket;
    protected ExpediusHL7LabHandler labHandler;
    protected ConfigurationBeanInterface configurationBean;
    protected String serviceName;
    protected ExpediusProperties properties;
    protected ExpediusMessageHandler messageHandler;
    protected int lastFileCount;
    protected ExpediusW3CDocumentHandler documentHandler;
    protected String labType;

    protected abstract boolean hostLogin();
    protected abstract void setLastFileCount(int lastFileCount);
    public abstract boolean start();
    public abstract void setConnection(ExpediusConnect connection); 
    public abstract void setServiceName(String serviceName);
    public abstract String getServiceName(); 
    public abstract void setMessageHandler(ExpediusMessageHandler messageHandler);
    public abstract void setDocumentHandler(ExpediusW3CDocumentHandler documentHandler);
      
    public AbstractConnectionController(ConfigurationBeanInterface configurationBean, ExpediusProperties properties) {

    	// contains static properties
    	if(properties != null) {
			this.properties = properties;
			TRUSTSTORE_URL = properties.getProperty("TRUSTSTORE_URL").trim();
			STORE_TYPE = properties.getProperty("STORE_TYPE").trim();
			STORE_PASS = properties.getProperty("STORE_PASS").trim();
			HTTPS_PROTOCOL = properties.getProperty("HTTPS_PROTOCOL").trim();
			KEYSTORE_URL = properties.getProperty("KEYSTORE_URL").trim();
			ACKNOWLEDGE_DOWNLOADS = properties.getProperty("ACKNOWLEDGE_DOWNLOADS").trim();
			
			setSocket( SSLSocket.getInstance(
					TRUSTSTORE_URL, 
					STORE_TYPE, 
					STORE_PASS, 
					HTTPS_PROTOCOL, 
					KEYSTORE_URL) );
        } 
    	
    	if(configurationBean != null) {
    		
    		setConfigurationBean(configurationBean);
    		
			USER = configurationBean.getUserName();
			PASS = configurationBean.getPassword();		
			URI = configurationBean.getServicePath();
			LOGIN = configurationBean.getLoginPath();	
			FETCH = configurationBean.getFetchPath();
			ACKNOWLEDGE = configurationBean.getAcknowledgePath();
			LOGOUT = configurationBean.getLogoutPath();
		} else {
			logger.warn("Missing configuration information.");
			return;
		}

    }

    public void setConfigurationBean(ConfigurationBeanInterface configurationBean) {
		logger.info("Setting configuration info.");
		this.configurationBean = configurationBean;		
	}
 
    public ConfigurationBeanInterface getConfigurationBean() {
    	return configurationBean;
    }

	protected ExpediusConnect getConnection() {
		return connection;
	}

	protected ExpediusHL7LabHandler getLabHandler() {
		return labHandler;
	}

	public void setLabHandler(ExpediusHL7LabHandler labHandler) {
		this.labHandler = labHandler;
	}
	
	public ExpediusMessageHandler getMessageHandler() {
		return this.messageHandler;
	}
	
	public SSLSocket getSSLSocket() {
		return this.socket;
	}
	
	public void setSocket(SSLSocket socket) {
		if(socket != null) {
			this.socket = socket;
		}
	}
	
	/**
	 * The number of labs last retrieved.
	 * @return number of labs.
	 */
	public int getLastFileCount() {
		return lastFileCount;
	}
	
	public ExpediusW3CDocumentHandler getDocumentHandler() {
		return this.documentHandler;
	}

    /**
     * Connection controllers start method executes the run method in 
     * all service controllers.
     * @param mode
     * @return
     */
    public boolean start(int mode) {

		boolean status = false;
		
		if(mode == LOGIN_MODE) {
			status = hostLogin();
			close();
		} else if (mode == DOWNLOAD_MODE) {
			run();
			status = true;
		}
		
		return status;
	}
    
    /**
     * close all streams and objects.
     */
    public void close() {
			
    	try {
    		if(getConnection() != null) {
	    		
				if( LOGOUT != null ){				
					getConnection().logout(LOGOUT);			
				}

				getConnection().close();	
    		}
    		
//    		ExpediusLog.close();
    		
		} catch (IOException e) {	
			handleError("Error closing connections during log-out. ", e, ERROR, false);				
		}
			
	}

    /**
	 * SSL Socket Factory for secure connection socket.
	 * @return
	 */
	protected SSLSocketFactory getSSLSocketFactory() {

		SSLSocketFactory socketFactory = null;
		String message = "Socket creation error during handshake. Failed to build custom Socket Factory "; 
		
		try {
			socketFactory = this.getSSLSocket().getSocketFactory();
		} catch (UnrecoverableKeyException e) {
			handleError(message, e, ERROR, true);
		} catch (KeyManagementException e) {
			handleError(message, e, ERROR, true);
		} catch (KeyStoreException e) {
			handleError(message, e, ERROR, true);
		} catch (NoSuchAlgorithmException e) {
			handleError(message, e, ERROR, true);
		} catch (CertificateException e) {
			handleError(message, e, ERROR, true);
		} catch (FileNotFoundException e) {
			handleError(message, e, ERROR, true);
		} catch (NoSuchProviderException e) {
			handleError(message, e, ERROR, true);
		} catch (IOException e) {
			handleError(message, e, ERROR, true);
		} finally {
			message = null;
		}

		return socketFactory;
	}
	
	/**
	 * SSL Context for secure server connections.
	 * @return
	 */
	protected SSLContext getSSLContext() {
		
		SSLContext socket = null;
		String message = "Socket creation error during handshake. The security certificate could be expired or corrupted. "; 
		
		try {
			socket = getSSLSocket().getSSlContext();
		} catch (UnrecoverableKeyException e) {
			handleError(message, e, ERROR, true);
		} catch (KeyManagementException e) {
			handleError(message, e, ERROR, true);
		} catch (KeyStoreException e) {
			handleError(message, e, ERROR, true);
		} catch (NoSuchAlgorithmException e) {
			handleError(message, e, ERROR, true);
		} catch (CertificateException e) {
			handleError(message, e, ERROR, true);
		} catch (IOException e) {
			handleError(message, e, ERROR, true);
		} catch (NoSuchProviderException e) {
			handleError(message, e, ERROR, true);
		} finally {
			message = null;
		}
		
		return socket;
		
	}
	
	/**
	 * Interpret server responses and trigger errors.
	 * Catches uncaught exceptions by reading server return status'
	 * @param serverResponse
	 */
	protected void processServerResponse(int serverResponse) {
		
		if(serverResponse == HttpsURLConnection.HTTP_GATEWAY_TIMEOUT) {
			handleError("A gateway timeout occurred while attempting to connect. Try again later. ", null, ERROR, false);
		}
		
		else if(serverResponse == HttpsURLConnection.HTTP_NOT_FOUND) {
			handleError("The server cannot be found. Ensure that the server address is correct. ", null, ERROR, false);
		}
		
		else if(serverResponse == HttpsURLConnection.HTTP_UNAUTHORIZED) {
			handleError("Failed authentication for this service. Ensure login data is correct. ", null, ERROR, false);				
		}
		
		else if(serverResponse == HttpsURLConnection.HTTP_INTERNAL_ERROR) {
			handleError("An internal error in Expedius server ceased operation. ", null, ERROR, false);	
		}
		
		else if(serverResponse == HttpsURLConnection.HTTP_FORBIDDEN) {			
			handleError("Failed authentication for this service. Access was forbidden. Ensure login data and security certificates are correct.", null, ERROR, false);			
		}
		
		else if(serverResponse == HttpsURLConnection.HTTP_UNAVAILABLE) {	
			handleError("Server for lab service is unavailable. Check network status. ", null, ERROR, true);			
		}
		
		else if(serverResponse == ExpediusHL7LabHandler.HTTP_WEBSERVICE_ERROR) {
			handleError("The server process has been halted due to an unknown communication error with Oscar. ", null, DISMISSABLE_ERROR, true);
		}
		
		else if(serverResponse > HttpsURLConnection.HTTP_NO_CONTENT) {
			handleError("The server process has been halted due to communication error. ", null, ERROR, false);
		}

	}
	
	protected boolean processResults() {
		return processResults(getDocumentHandler().getDocument(), getLabType()); 
	}

	/**
	 * Do all actions to get HL7 files into Oscar.
	 * This method changes the HTTP status to ExpediusConnectionController.HTTP_ERROR_THROWN [-1] 
	 * so that 
	 * @param results
	 */
	protected boolean processResults(Document results, String labType) {

		setLastFileCount( getDocumentHandler().getMessageCount() );
		setLabType(labType);
		
		logger.info( getLastFileCount()  + " " + getLabType() + " lab files found." );

		boolean success = Boolean.FALSE;
		
		// exit if no lab files were downloaded.
		if ( getLastFileCount() > 0 ) {

			if( getLabHandler() != null ) {
				
				logger.info("Saving and parsing " + getLastFileCount() + " lab files.");
				
				// set the local file system save path for the hl7 file.
				getLabHandler().setFileName(getConfigurationBean().getServiceName() + "." + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

				// point lab results to the handler.
				getLabHandler().setHl7labs(results);
				getLabHandler().setLabType(labType);
				
				// save the results file
				try {		
					success = getLabHandler().saveFile(); 	
				} catch (IOException e) {
					handleError("Expedius has failed to save downloaded lab files. They will not be aknowledged. Contact support. ", e, ERROR, true);
				} catch (TransformerException e) {								
					handleError("There was a problem parsing the lab files into XML. They have not been saved and will not be aknowledged. ", e, ERROR, true);				
				} finally {
					if( getLabHandler().getResponseCode() == ExpediusHL7LabHandler.OK ) {	
						logger.info(getServiceName() + " lab files have been saved to local file system: " + getLabHandler().getSavePath() + getLabHandler().getFileName());			
					}
				}

			} else {
				handleError("A software error prevented the lab file from being saved ", null, DISMISSABLE_ERROR, true);
			}
			
		} 
		
		return success;
	}
	
	protected void parseAndPersist(String labType) {
		setLabType(labType);
		parseAndPersist();
	}
	
	protected void parseAndPersist() {
		
		if( getLabHandler().getResponseCode() == HttpsURLConnection.HTTP_OK ) {

			try {
				getLabHandler().saveHL7();				
			} catch (FileNotFoundException e) {
				handleError(" There was a problem with persisting lab files to the Oscar database. The target file was not found. ", e, DISMISSABLE_ERROR, true);
			} catch (RemoteException e) {
				handleError(" Oscar server is unreachable while loading lab files into the database. Is the webservice configured correctly? ", e, DISMISSABLE_ERROR, true);
			} catch (IOException e) {
				handleError(" A file communication error occured while persisting lab files to the Oscar database.", e, DISMISSABLE_ERROR, true);
			}  finally {
				if(getLabHandler().getResponseCode() == ExpediusHL7LabHandler.OK) {					
					logger.info("All " + getServiceName() + " lab Files Transfered and Parsed to Oscar successfully.");
					// reset lab handler.
					getLabHandler().reset();
				}
			}			
		}
	}

	/**
	 * Processes errors and exceptions by creating a log entry, sending a user GUI message, 
	 * and alerting the user by email.
	 *
	 */
	protected void handleError(String message, Exception exception, int errorLevel, boolean sendEmail) {
	
		String fileLocation = getLabHandler().getSavePath();

		String fileName = getLabHandler().getFileName();
		String serviceName = getServiceName();
		
		StringBuilder finalMessage = new StringBuilder();
		
		if(serviceName != null) {
			finalMessage.append(" Service " + serviceName + " has failed. ");
		}
		
		if(exception != null) {
			finalMessage.append(" Due to a fatal exception error " + exception.getMessage() + ". ");
		}
		
		if(message != null) {
			finalMessage.append(message);
		}
		
		if(fileName != null) {
			finalMessage.append(" File location: " + fileLocation);
			finalMessage.append(fileName);
		}

		logger.log(Level.ERROR, finalMessage.toString(), exception);
		logger.log(Level.INFO, "HTTP connection status " + getConnection().getResponseCode());
		logger.log(Level.INFO, "Web Service status " + getLabHandler().getResponseCode());
		
		if(errorLevel == ERROR) {
			getMessageHandler().addErrorMessage(finalMessage.toString());
		}
		
		if(errorLevel == DISMISSABLE_ERROR) {
			getMessageHandler().addDismissableErrorMessage(finalMessage.toString());
		}
		
		if(sendEmail) {				
			Email.sendEmail("Expedius Lab Download Error", finalMessage.toString(), properties);
		}

	}
	
	public String getLabType() {
		return labType;
	}
	
	public void setLabType(String labType) {
		this.labType = labType;
	}

}
