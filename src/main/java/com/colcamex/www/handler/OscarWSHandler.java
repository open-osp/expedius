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

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;

import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.log4j.Logger;
import org.oscarehr.ws.LabUploadWs;
import org.oscarehr.ws.LabUploadWsService;
import org.oscarehr.ws.LoginResultTransfer2;
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

	private final static String LOGIN_SERVICE = ExpediusProperties.getProperties().getProperty("EMR_LOGIN_ENDPOINT").trim();
	private final static String LAB_UPLOAD_SERVICE = ExpediusProperties.getProperties().getProperty("EMR_LAB_UPLOAD_ENDPOINT").trim();
	private final static String HOST = ExpediusProperties.getProperties().getProperty("EMR_HOST_NAME").trim();
	private final static String CONTEXT = ExpediusProperties.getProperties().getProperty("EMR_CONTEXT_PATH").trim();
	private final static String WEB_SERVICE = ExpediusProperties.getProperties().getProperty("EMR_SERVICE_ENDPOINT").trim();
	private final static Boolean SSL_ENABLED  = Boolean.parseBoolean(ExpediusProperties.getProperties().getProperty("EMR_SSL_ENABLED").trim());
	private final static URL LOGIN_WSDL = OscarWSHandler.class.getResource("LoginService.wsdl");
	private final static URL LAB_UPLOAD_WSDL = OscarWSHandler.class.getResource("LabUploadService.wsdl");


	private LabUploadWs hL7LabUploadWs;
	private LoginWs loginWs;
	private String username;
	private String password;
	private String providerNumber;

	@SuppressWarnings("rawtypes")
	public OscarWSHandler() {
		
		if(ExpediusProperties.getProperties().containsKey("SERVICE_NUMBER")) {
			providerNumber = ExpediusProperties.getProperties().getProperty("SERVICE_NUMBER").trim();
		}
		if(providerNumber == null || providerNumber.isEmpty()) {
			return;
		}

		System.setProperty( "com.sun.net.ssl.enableECC", "false");
		
		String protocol = SSL_ENABLED ? "https" : "http"; 
		String endpointAddress = String.format("%1$s://%2$s/%3$s/%4$s/", protocol, HOST, CONTEXT, WEB_SERVICE);

		LoginWsService loginWsService = new LoginWsService(LOGIN_WSDL);
		loginWs = loginWsService.getLoginWsPort();
		BindingProvider provider = (BindingProvider) loginWs;
		provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress + LOGIN_SERVICE);
		
		LabUploadWsService labUploadWsService = new LabUploadWsService(LAB_UPLOAD_WSDL);	
		hL7LabUploadWs = labUploadWsService.getLabUploadWsPort();		
		provider = (BindingProvider)hL7LabUploadWs;
		provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress + LAB_UPLOAD_SERVICE);	
		
		List<Handler> handlerChain = provider.getBinding().getHandlerChain();		
		handlerChain.add(new OscarWSAuthHandler());
		provider.getBinding().setHandlerChain(handlerChain);

	}

	public OscarWSHandler(String username, String password ) {
		this();
		setUsername( username ); 
		setPassword( password );
	}

	private LoginResultTransfer2 login() {
		return login( getUsername(), getPassword() );
	}
	
	/**
	 * Authenticate and then add the returned token to the SOAP handler chain.
	 * @param user - full login username
	 * @param pass - full user password
	 */
	private LoginResultTransfer2 login( String user, String pass ) {

		if( loginWs != null ) {	
			try {
				logger.debug("Logging In");
				configureSSLConduit( loginWs );
				return loginWs.login2(user, pass);
			} catch (Exception e) {
				logger.error("Unauthorized access ", e);
			}		
		}

		return null;
	}

	public String saveHL7(String savedFileName, String fileContent) {
		logger.debug("SaveHL7 " + savedFileName);
		configureSSLConduit( hL7LabUploadWs );
		injectAuthenticationToken( hL7LabUploadWs );
		logger.debug("Service Number (provider)" + providerNumber);
		return hL7LabUploadWs.uploadExcelleris(savedFileName, fileContent, this.providerNumber);
	}

	/**
	 * Get the message handler; OscarWSAuthHandler and put the authentication token into 
	 * the header of this call. 
	 * 
	 * @param hL7LabUploadWs
	 */
	@SuppressWarnings("rawtypes")
	private void injectAuthenticationToken( LabUploadWs hL7LabUploadWs ){
		LoginResultTransfer2 loginResultTransfer = login();
		if(loginResultTransfer != null)
		{
			BindingProvider bindingProvider = (BindingProvider) hL7LabUploadWs;
			List<Handler> handlerChain = bindingProvider.getBinding().getHandlerChain();
			for( Handler handler : handlerChain ){		
				if( handler instanceof OscarWSAuthHandler ) {
					( ( OscarWSAuthHandler ) handler).setLoginResultTransfer( loginResultTransfer );
				}
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
			httpConduit.getClient().setContentType("multipart/form-data");
			
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
