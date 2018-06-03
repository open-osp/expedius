package com.colcamex.www.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import org.apache.log4j.Logger;
import org.oscarehr.ws.Exception_Exception;
import org.oscarehr.ws.HL7LabUploadWs;
import org.oscarehr.ws.HL7LabUploadWsService;
import org.oscarehr.ws.LoginResultTransfer;
import org.oscarehr.ws.LoginWs;
import org.oscarehr.ws.LoginWsService;

import com.colcamex.www.security.SSLSocket;
import com.colcamex.www.util.ExpediusProperties;

/**
 * @author Dennis Warren 
 * @ Colcamex Resources
 * dwarren@colcamex.com
 * www.colcamex.com 
 * Date: Jan 2016.
 * 
 */
public class OscarWSHandler {

	public static Logger logger = Logger.getLogger( OscarWSHandler.class );
	
	private final static String EMR_WEB_SERVICE = "https://192.168.2.191:8181/oscar-12.1.1/ws/";
	// private final static String EMR_WEB_SERVICE = "http://localhost:8080/oscar-SNAPSHOT/ws/";
	private final static String LOGIN_SERVICE = ExpediusProperties.getInstance().getProperty("EMR_LOGIN_ENDPOINT");
	private final static String LAB_UPLOAD_SERVICE = ExpediusProperties.getInstance().getProperty("EMR_LAB_UPLOAD_ENDPOINT");
	private final static URL LOGIN_WSDL = OscarWSHandler.class.getResource("loginService.wsdl");
	private final static URL LAB_UPLOAD_WSDL = OscarWSHandler.class.getResource("HL7LabUpload.wsdl");
	private final static String LOGIN_SERVICE_URL = EMR_WEB_SERVICE + LOGIN_SERVICE;
	private final static String LAB_UPLOAD_SERVICE_URL = EMR_WEB_SERVICE + LAB_UPLOAD_SERVICE;

	private HL7LabUploadWs hL7LabUploadWs;
	private LoginWs loginWs;
	private String username;
	private String password;

	public OscarWSHandler() {

		BindingProvider provider = null;

		// System.setProperty( "javax.net.ssl.trustStorePassword", "changeit" );
		// System.setProperty( "javax.net.ssl.keyStorePassword", TRUST_STORE_PASSWORD );
		
		System.setProperty( "com.sun.net.ssl.enableECC", "false");
		
		LoginWsService loginWsService = new LoginWsService(LOGIN_WSDL);
		loginWs = loginWsService.getLoginWsPort();

		provider = (BindingProvider)loginWs;
		provider.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, LOGIN_SERVICE_URL  );

		HL7LabUploadWsService hL7LabUploadWsService = new HL7LabUploadWsService(LAB_UPLOAD_WSDL);	
		hL7LabUploadWs = hL7LabUploadWsService.getHL7LabUploadWsPort();
		
		provider = (BindingProvider)hL7LabUploadWs;
		provider.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, LAB_UPLOAD_SERVICE_URL  );	

	}

	public OscarWSHandler(String username, String password ) {
		this();
		setUsername( username ); 
		setPassword( password );
	}

	public LoginResultTransfer login() {
		return login( getUsername(), getPassword() );
	}
	
	/**
	 * Authenticate and then add the returned token to the SOAP handler chain.
	 * @param user - full login username
	 * @param pass - full user password
	 */
	private LoginResultTransfer login( String user, String pass ) {

		if( loginWs != null ) {			
			logger.debug("Logging In");
			configureSSLConduit( loginWs );
			return loginWs.login(user, pass);
		}

		return null;
	}

	public boolean parseHL7( String className, String savedPath, int fileId, String labType ) {
		logger.debug("ParseHL7 Handler");
		injectAuthenticationToken( hL7LabUploadWs );
		return hL7LabUploadWs.parseHL7(className, savedPath, fileId, labType);
	}

	public int saveHL7( String savedFileName, String providerNumber ) throws Exception_Exception {
		logger.debug("SaveHL7 Handler");
		configureSSLConduit( hL7LabUploadWs );
		injectAuthenticationToken( hL7LabUploadWs );		
		return hL7LabUploadWs.saveHL7( savedFileName, providerNumber );
	}

	private void injectAuthenticationToken( HL7LabUploadWs hL7LabUploadWs ) {
		// login is done for each end point.
		LoginResultTransfer loginResultTransfer = login( getUsername(), getPassword() );
		// set the authentication header
		BindingProvider bindingProvider = (BindingProvider) hL7LabUploadWs;
		List<Handler> handlerChain = bindingProvider.getBinding().getHandlerChain();
		for( Handler handler : handlerChain ){
			if( handler instanceof OscarWSAuthHandler ) {
				( ( OscarWSAuthHandler ) handler).setLoginResultTransfer( loginResultTransfer );
			}
		}
	}
	
	private void configureSSLConduit( Object port ) {
		
		HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();
		TLSClientParameters tlsCP = new TLSClientParameters();
		try {

			// configure with the Expedius custom Socket Factory.
			
			tlsCP.setSSLSocketFactory( SSLSocket.getInstance().getSSlContext().getSocketFactory() );
			tlsCP.setSslCacheTimeout(300000);
			tlsCP.setDisableCNCheck(Boolean.TRUE);
			httpConduit.setTlsClientParameters(tlsCP);
			
		} catch (UnrecoverableKeyException e) {
			logger.error("", e);
		} catch (KeyManagementException e) {
			logger.error("", e);
		} catch (KeyStoreException e) {
			logger.error("", e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("", e);
		} catch (CertificateException e) {
			logger.error("", e);
		} catch (FileNotFoundException e) {
			logger.error("", e);
		} catch (NoSuchProviderException e) {
			logger.error("", e);
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	private String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	private String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
